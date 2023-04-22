(ns calculator.core
  (:require
   [goog.dom :as dom]
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
   "C" (constantly 0)})

(defn append-number-to-display-number
  "Clicking a number will append the clicked number to the display number"
  [user-input-number newly-clicked-number]
  (cond
    (> user-input-number 9999999) "OVERFLOW"
    (and (zero? (js/parseFloat user-input-number)) (zero? (js/parseFloat newly-clicked-number))) "0"
    (or (= user-input-number "OVERFLOW")
        (zero? (js/parseFloat user-input-number))) newly-clicked-number
    :else (str user-input-number newly-clicked-number)))

(defn apply-old-operation
  "Clicking a new operation means calling the old operation on total & display-number"
  [total display-number previous-operation]
  (let [f (get op->fn previous-operation)]
    (if (nil? f)
      display-number
      (let [v (f (js/parseFloat total) (js/parseFloat display-number))]
        (cond
          (> (max v (- v)) 9999999) "OVERFLOW"
          (> (count (str v)) 8) (subs (str v) 0 8)
          :else (str v))))))

(def keys-container (dom/getElement "keys"))
(def number-display (dom/getElement "number-display"))
(def operator-display (dom/getElement "operator-display"))

(defn set-number-and-operator-displays! [number-value operator-value]
  (dom/setProperties number-display #js {:textContent number-value})
  (dom/setProperties operator-display #js {:textContent operator-value}))

(defn set-state! [total-v old-temp-storage-v old-operator-v new-temp-storage-v new-operator-v last-clicked-v]
  (reset! total (apply-old-operation total-v old-temp-storage-v old-operator-v))
  (reset! temp-storage new-temp-storage-v)
  (reset! operator new-operator-v)
  (reset! last-clicked last-clicked-v))

(defn create-calculator-keys! []
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
                               (set-number-and-operator-displays! @temp-storage @operator)))

              (contains? operators clicked-btn)
              (events/listen new-div "click"
                             (fn []
                               (if (= @last-clicked :operator)
                                 (do (reset! operator clicked-btn)
                                     (set-number-and-operator-displays! @total @operator))
                                 (do (set-state! @total @temp-storage @operator @temp-storage clicked-btn :operator)
                                     (set-number-and-operator-displays! @total @operator)))))

              (= clicked-btn "=")
              (events/listen new-div "click"
                             (fn []
                               (set-state! @total @temp-storage @operator @total nil :operator)
                               (set-number-and-operator-displays! @total @operator)))

              (= clicked-btn "C")
              (events/listen new-div "click"
                             (fn []
                               (set-state! @total @temp-storage "C" 0 nil nil)
                               (set-number-and-operator-displays! @total @operator))))

            (dom/appendChild keys-container new-div)))
        button-types))

(defn init []
  (create-calculator-keys!))
