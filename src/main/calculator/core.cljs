(ns calculator.core
  (:require [goog.dom :as dom]
            #_[goog.events :as events]))

(def total (atom 0))
(def temp-input-zone (atom nil))
(def operator (atom nil))

(defn event-target-value [e]
  (.-value (.-target e)))

(def button-types ["7" "8" "9" "+" "4" "5" "6" "-" "1" "2" "3" "x" "C" "0" "=" "/"])

(defn create-calculator-keys []
  (let [keys-container (dom/getElement "keys")]
    (doall (for [button-type button-types
                 :let [new-div (dom/createElement "button")]]
             (do
               (dom/setProperties new-div #js {:class "glow-on-hover"
                                               :id button-type
                                               :textContent button-type})
               (dom/appendChild keys-container new-div))))))

(defn init []
  (create-calculator-keys))
