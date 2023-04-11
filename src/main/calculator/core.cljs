(ns calculator.core
  (:require [goog.dom :as dom]
            [goog.events :as events]))

(def total (atom 0))
; (def temp-input-zone (atom nil))
(def operator (atom nil))

(defn event-target-value [e]
  (.-value (.-target e)))

(def button-types ["7" "8" "9" "+" "4" "5" "6" "-" "1" "2" "3" "x" "C" "0" "=" "/"])

(def div-id->meaning
  {"1" 1
   "2" 2
   "3" 3
   "4" 4
   "5" 5
   "6" 6
   "7" 7
   "8" 8
   "9" 9
   "0" 0
   "+" (fn [total x] (+ total x))
   "-" (fn [total x] (- total x))
   "*" (fn [total x] (* total x))
   "/" (fn [total x] (/ total x))
   "C" (constantly 0)})

;user presses 54
;that value is kept in id=number
;user presses +
;=> at this moment, store number in total
;=> AND store "+" as operator
;=> AND clear the div id=number

;for clicking numbers
; this will happen every time a number is clicked
(defn append-number-to-display-number
  "Clicking a number will append the clicked number to the display number"
  [display-number clicked-number]
  (cond
    (> display-number 99999999) "OVERFLOW"
    (and (zero? (int display-number)) (zero? (int clicked-number))) "0"
    (zero? (int display-number)) clicked-number
    :else (str display-number clicked-number)))

;; this will happen
;; the value of this fn call will be set to the atom called total
;; the operation atom will be replaced with the new operation
(defn apply-old-operation
  "Clicking a new operation means calling the old operation on total & display-number"
  [total display-number previous-operation]
  (let [f (get div-id->meaning previous-operation)]
    (f (int total) (int display-number))))

(comment
  ;test zone
  (apply-old-operation "12" "3" "+")
  (append-number-to-display-number "123443" "9")
  (append-number-to-display-number "0" "5")
  (append-number-to-display-number "0" "0"))

(defn create-calculator-keys []
  (let [keys-container (dom/getElement "keys")
        number-display (dom/getElement "number-display")
        operator-display (dom/getElement "operator-display")]
    (doall (for [button-type button-types
                 :let [new-div (dom/createElement "button")
                       fn? (not (int? (get div-id->meaning button-type)))]]
             (do
               (dom/setProperties new-div #js {:class "glow-on-hover"
                                               :id button-type
                                               :textContent button-type})
               (if (not fn?)
                 (events/listen new-div "click" #(dom/setProperties number-display #js {:textContent button-type}))
                 (events/listen new-div "click" #(dom/setProperties operator-display #js {:textContent button-type})))
               (dom/appendChild keys-container new-div))))))

(defn init []
  (create-calculator-keys))
