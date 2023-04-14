(ns calculator.core
  (:require [goog.dom :as dom]
            [goog.events :as events]))

(def total (atom 0))
(def user-input (atom 0))
(def operator (atom nil))
(def last-clicked (atom nil))

(def button-types ["7" "8" "9" "+" "4" "5" "6" "-" "1" "2" "3" "x" "C" "0" "=" "/"])
(def operators #{"-" "+" "x" "/"})
(def numbers #{"0" "1" "2" "3" "4" "5" "6" "7" "8" "9"})

(def op->fn
  {"+" (fn [total x] (+ total x))
   "-" (fn [total x] (- total x))
   "x" (fn [total x] (* total x))
   "/" (fn [total x] (/ total x))})

(defn append-number-to-display-number
  "Clicking a number will append the clicked number to the display number"
  [user-input-number newly-clicked-number]
  (cond
    (> user-input-number 9999999) "OVERFLOW"
    (and (zero? (int user-input-number)) (zero? (int newly-clicked-number))) "0"
    (zero? (int user-input-number)) newly-clicked-number
    :else (str user-input-number newly-clicked-number)))

(defn apply-old-operation
  "Clicking a new operation means calling the old operation on total & display-number"
  [total display-number previous-operation]
  (let [f (get op->fn previous-operation)]
    (if (nil? f)
      display-number
      (str (f (int total) (int display-number))))))

;; above, all pure functions & data
;; below, side fx and impure functions

(defn create-calculator-keys! []
  (let [keys-container (dom/getElement "keys")
        number-display (dom/getElement "number-display")
        operator-display (dom/getElement "operator-display")]
    (doall (for [clicked-btn button-types
                 :let [new-div (dom/createElement "button")]]
             (do
               (dom/setProperties new-div #js {:class "glow-on-hover"
                                               :id clicked-btn
                                               :textContent clicked-btn})
               (cond
                 (contains? numbers clicked-btn)
                 (events/listen
                  new-div "click"
                  (fn [_]
                    (dom/setProperties number-display #js {:textContent (if (= @last-clicked :operator)
                                                                          (do
                                                                            (reset! user-input clicked-btn)
                                                                            @user-input)
                                                                          (do
                                                                            (reset! user-input (append-number-to-display-number @user-input clicked-btn))
                                                                            @user-input))})
                    (reset! last-clicked :number)))
                 (contains? operators clicked-btn)
                 (events/listen
                  new-div "click"
                  #(do
                     (let [result (apply-old-operation @total @user-input @operator)]
                       (reset! total result)
                       (dom/setProperties number-display #js {:textContent @total})
                       (reset! operator clicked-btn)
                       (dom/setProperties operator-display #js {:textContent clicked-btn})
                       (reset! last-clicked :operator))))

                 (= clicked-btn "=")
                 (events/listen new-div "click"
                                #(do
                                   (reset! total (apply-old-operation @total @user-input @operator))
                                   (reset! user-input 0)
                                   (reset! operator nil)
                                   (reset! last-clicked nil)
                                   (dom/setProperties number-display #js {:textContent @total})
                                   (dom/setProperties operator-display #js {:textContent @operator})))

                 (= clicked-btn "C")
                 (events/listen new-div "click"
                                #(do
                                   (reset! total 0)
                                   (reset! user-input 0)
                                   (reset! operator nil)
                                   (reset! last-clicked nil)
                                   (dom/setProperties number-display #js {:textContent @total})
                                   (dom/setProperties operator-display #js {:textContent @operator})))

                 :else (constantly nil))

               (dom/appendChild keys-container new-div))))))

(defn init []
  (create-calculator-keys!))
