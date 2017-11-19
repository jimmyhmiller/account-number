(ns account-number.core
  (:require [clojure.string :as string]
            [cuerdas.core :refer [<<-]]))
(def reference-digits
  (<<- " _     _  _     _  _  _  _  _ 
        | |  | _| _||_||_ |_   ||_||_|
        |_|  ||_  _|  | _||_|  ||_| _|"))

(defn get-ascii-digit [rows x]
  (for [y (range 0 3)
        x (range (* x 3) (+ (* x 3) 3))]
    (get-in rows [y x])))

(defn remove-unnecessary-segments [[_ a _ & rest]]
  (into [a] rest))

(defn to-seven-segment [ascii-digit]
  (->> ascii-digit
       remove-unnecessary-segments
       (map #(not= " " %))
       (into [])))

(defn rows->seven-segment [rows]
  (->> (range (/ (count (first rows)) 3))
       (map (partial get-ascii-digit rows))
       (map to-seven-segment)))

(defn split-each-row [row]
  (->> row
       (map #(string/split % #""))
       (into [])))

(defn split-into-rows [contents]
  (->> (string/split contents #"\n")
       (partition 3)
       (map split-each-row)))

(def int->seven-segment 
  (->> reference-digits
       split-into-rows
       (map rows->seven-segment)
       first
       (into [])))

(def seven-segment->int 
  (->> (range)
       (map vector int->seven-segment)
       (into {})))

(defn seven-segment->account-number [coll]
  (mapv seven-segment->int coll))

(defn check-sum [numbers]
  (let [sum (->> (range 1 10)
                 (map * (reverse numbers))
                 (reduce +))]
    (mod sum 11)))

(defn has-invalid-digits? [numbers]
  (not-every? (complement nil?) numbers))

(defn valid-account-number? [numbers]
  (and (not (has-invalid-digits? numbers))
       (zero? (check-sum numbers))))

(defn determine-error-message [numbers]
  (let [has-invalid-numbers (has-invalid-digits? numbers)
        is-valid (valid-account-number? numbers)]
    (cond
      has-invalid-numbers " ILL" 
      (not is-valid) " ERR" 
      :else "")))

(defn format-output [numbers]
  (let [error-message (determine-error-message numbers)]
    (as-> numbers n
      (map (fnil identity "?") n)
      (string/join n)
      (str n error-message))))

(defn hamming-distance [segment1 segment2]
  {:pre [(= (count segment1) (count segment2))]}
  (->> (map vector segment1 segment2)
       (filter (fn [[x y]] (not= x y)))
       count))

(defn find-possible-digits [segment]
  (filter #(= (hamming-distance segment %) 1) int->seven-segment))

(defn find-replacements [account-number]
  (->> (map find-possible-digits account-number)
       (map-indexed (fn [index replacements] (map vector (repeat index) replacements)))
       (mapcat identity)))

(defn try-possibility [account-number [index value]]
  (assoc account-number index value))




(defn generate-possible-account-numbers [account-number]
  (let [replacements (find-replacements account-number)]
    (->> (map (partial try-possibility account-number) replacements)
         (map seven-segment->account-number)
         (cons (seven-segment->account-number account-number))
         (filter valid-account-number?))))

(map #(map seven-segment->int %) (map #(find-possible-digits (int->seven-segment %)) (range 10)))


(valid-account-number? [4 9  0 6 7 1 8 5])

(->> [4 9 0 0 6 7 7 1 5]
     (map int->seven-segment)
     (into [])
     find-replacements)
