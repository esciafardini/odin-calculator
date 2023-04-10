(ns calculator.core
  (:require [goog.dom :as dom]
            #_[goog.events :as events]))

(def total (atom 0))
(def temp-input-zone (atom nil))
(def operator (atom nil))

(defn event-target-value [e]
  (.-value (.-target e)))

(def button-types ["7" "8" "9" "+" "4" "5" "6" "-" "1" "2" "3" "x" "C" "0" "=" "/"])

(defn could-it-be []
  (let [keys-container (dom/getElement "keys")]
    (doall (for [button-type button-types
                 :let [new-div (dom/createElement "button")]]
             (do
               (dom/classlist.add new-div "glow-on-hover")
               (set! (.. new-div -id) button-type)
               (set! (.. new-div -textContent) button-type)
               (dom/appendChild keys-container new-div))))))

(defn init []
  (could-it-be))
