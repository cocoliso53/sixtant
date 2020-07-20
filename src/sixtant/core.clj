(ns sixtant.core
  (:require [clojure.data.json :as json]
            [gniazdo.core :as ws]
            [clojure.edn :as edn]
            [sixtant.db :as c]
            [ruiyun.tools.timer :as t])
  (:gen-class))

(def websocket "wss://ws.bitstamp.net/")
(def sub-dict {:event "bts:subscribe"
               :data {:channel "order_book_btcusd"}})

;; Helper function
(defn sum-qty [pair]
  "Returns the sum of qty from a list of the form
   pair = [[price qty]]"
  (reduce + (map #(nth % 1) pair)))

;; Only to be used as fun in split-coll
(defn check-qty [target pair]
  (<= target (sum-qty pair))) 

;; Selects only the pairs (price amount) that are relevant
(defn split-coll [fun coll n]
  (cond
    (fun (take n coll)) (take n coll)
    (> (count coll) n) (split-coll fun coll (+ n 1))
    :else (take n coll)))

;; Functions to calculate weighted average
(defn price-times-qty [pairs]
  (let [[price qty] pairs]
    (* price qty)))

(defn weight-last [pairs target]
  (let [body (drop-last pairs)
        [price-last _] (last pairs)]
    (* price-last (- target (sum-qty body)))))

(defn full-weight [pairs target]
  (let [body (drop-last pairs)
        weights-body (map price-times-qty body)]
    (+ (reduce + weights-body)
       (weight-last pairs target))))

(defn wamp [asks bids target] ;here I had some confusion on the definition of wamp
  (let [asks-wmp (/ (full-weight asks target) target)
        bids-wmp (/ (full-weight bids target) target)]
    (/ (+ asks-wmp bids-wmp) 2)))

(defn signal [ask-min bid-max wamp]
  (cond
    (< ask-min wamp) "BUY"
    (> bid-max wamp) "SELL"
    :else "NONE"))


;; Socket connection
(defn connect-socket [websocket sub-dict]
  (let [socket (ws/connect websocket
                           :on-receive #(-> %
                                            (json/read-str :key-fn
                                                           keyword)
                                            (c/insert-db c/node)))]
    (ws/send-msg socket (json/write-str sub-dict))))

;; Clean data and sort from db
;; and sort from cheapes to most expensive
;; NOTE: bids list must be reversed
(defn str-float [pairs]
  (let [clean (map #(map edn/read-string %) pairs)]
    (sort-by first clean)))

;;Gets data from the db and does a first cleaning
(defn result-raw []
  (let [book (c/recent-book c/node)
        ts (-> (:microtimestamp book) bigint (/ 1000) Math/floor bigint)
        asks (str-float (:asks book))
        bids (reverse (str-float (:bids book)))]
    (assoc {} :ts ts :asks asks :bids bids)))

;; Argument is the resuting hashmap from result-raw
;; Does final cleaning and procesing. Rturn a hashmap in the required format
(defn result [{:keys [ts asks bids]}]
  (let [ask-min (ffirst asks)
        bid-max (ffirst bids)
        ask-relevant (split-coll #(check-qty 5 %) asks 1)
        bid-relevant (split-coll #(check-qty 5 %) bids 1)
        btc-mid (wamp ask-relevant bid-relevant 5)]
    (assoc {}
           :ask ask-min
           :bid bid-max
           :btc-mid btc-mid
           :signal (signal ask-min bid-max btc-mid)
           :timestamp ts)))

(defn print-result []
  (let [r (result (result-raw))]
    (json/write-str r)))

(defn -main [& args]
  "I don't do a whole lot ... yet."
  (connect-socket websocket sub-dict)
  (def sixtant-timer (t/timer "sixtant"))
  (t/run-task! #(println (print-result))
               :delay 3000
               :period 1000
               :by sixtant-timer))
