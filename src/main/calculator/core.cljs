(ns calculator.core
  (:require [goog.dom :as dom]
            [goog.events :as events]))

(def total (atom 0))
(def temp-storage (atom 0))
(def operator (atom nil))
(def last-clicked (atom nil))

(def button-types ["7" "8" "9" "+" "4" "5" "6" "-" "1" "2" "3" "x" "C" "0" "=" "/"])
(def operators #{"-" "+" "x" "/"})
(def numbers #{"0" "1" "2" "3" "4" "5" "6" "7" "8" "9"})

(def op->fn
  {"+" (fn [total x] (+ total x))
   "-" (fn [total x] (- total x))
   "x" (fn [total x] (* total x))
   "/" (fn [total x] (/ total x))
   "C" (fn [_ _] 0)})

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

(defn create-calculator-keys! []
  (let [keys-container (dom/getElement "keys")
        number-display (dom/getElement "number-display")
        operator-display (dom/getElement "operator-display")]
    (mapv (fn [clicked-btn]
            (let [new-div (dom/createElement "button")]
              ;add class & attrs to each button
              (dom/setProperties new-div #js {:class "glow-on-hover"
                                              :id clicked-btn
                                              :textContent clicked-btn})
              ;add the side-fx to each button based on type
              (cond
                (contains? numbers clicked-btn)
                (events/listen new-div "click"
                               (fn [_]
                                 (if (= @last-clicked :operator)
                                   (reset! temp-storage clicked-btn) ;set temp storage to new number
                                   (reset! temp-storage (append-number-to-display-number @temp-storage clicked-btn)))
                                 (reset! last-clicked :number)
                                 (dom/setProperties number-display #js {:textContent @temp-storage})))

                (contains? operators clicked-btn)
                (events/listen new-div "click"
                               (fn []
                                 (if (= @last-clicked :operator)
                                   (do (reset! operator clicked-btn)
                                       (dom/setProperties operator-display #js {:textContent @operator}))
                                   (do (reset! total (apply-old-operation @total @temp-storage @operator))
                                       (reset! temp-storage @temp-storage) ;no-op
                                       (reset! operator clicked-btn)
                                       (reset! last-clicked :operator)
                                       (dom/setProperties number-display #js {:textContent @total})
                                       (dom/setProperties operator-display #js {:textContent @operator})))))

                (= clicked-btn "=")
                (events/listen new-div "click"
                               (fn []
                                 (reset! total (apply-old-operation @total @temp-storage @operator))
                                 (reset! temp-storage @total)
                                 (reset! operator nil)
                                 (reset! last-clicked :operator)
                                 (dom/setProperties number-display #js {:textContent @total})
                                 (dom/setProperties operator-display #js {:textContent @operator})))

                (= clicked-btn "C")
                (events/listen new-div "click"
                               (fn []
                                 (reset! total (apply-old-operation @total @temp-storage "C"))
                                 (reset! temp-storage 0)
                                 (reset! operator nil)
                                 (reset! last-clicked nil)
                                 (dom/setProperties number-display #js {:textContent @total})
                                 (dom/setProperties operator-display #js {:textContent @operator}))))

              (dom/appendChild keys-container new-div)))
          button-types)))

(defn init []
  (create-calculator-keys!))
