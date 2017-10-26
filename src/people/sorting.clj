(ns people.sorting)

(defn- asc [c] c)

(defn- desc [compare-to]
  (fn [a b]
    (* -1 (compare-to a b))))

(defn- field-comparator [field]
  (fn [a b]
    (.compareTo (field a) (field b))))

(defn sort-by-gender [records]
  (sort
    (fn [a b]
      (or
        (some
          #(if (zero? %) false %)
          (map #(% a b) [(asc (field-comparator :gender))
                         (asc (field-comparator :last-name))]))
        0))
    records))


(defn sort-by-birthdate [records]
  (sort
    (asc (field-comparator :birthdate))
    records))

(defn sort-by-last-name [records]
  (sort
    (desc (field-comparator :last-name))
    records))

(def sorting-config
  {:gender {:field :gender
            :direction :asc
            :sort-records sort-by-gender}
   :last-name {:field :last-name
               :direction :desc
               :sort-records sort-by-last-name}
   :birthdate {:field :birthdate
               :direction :asc
               :sort-records sort-by-birthdate}})
