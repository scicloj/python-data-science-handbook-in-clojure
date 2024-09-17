;; # Understanding Data Types in Clojure DRAFT

;; [original chapter](https://jakevdp.github.io/PythonDataScienceHandbook/02.01-understanding-data-types.html)

(ns pdshic.02-01-understanding-data-types-in-clojure
  (:require [pdshic.util :as util]
            [clojure.math :as math]
            [fastmath.random :as rand]
            [scicloj.kindly.v4.kind :as kind]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.tensor :as dtt]
            [tech.v3.datatype.functional :as fun]))

;; Like Python, Clojure enjoys the benefits of dynamic typing.
;; In Python any data can be assigned to any variable:

(util/quote-python "
x = 4
x = \"four\"
")

;; Clojure Vars are akin to Python variables.
;; When you use the `def` special form you create a Var
;; and [intern](https://clojure.org/reference/vars#interning)
;; it under the provided symbol.

;; The first argument to `def` is the symbol we're assigning it to.

^:kindly/hide-code
(kind/hidden (ns-unmap *ns* 'x))
(def x)

;; Here we've created an unbound Var and assigned it to the symbol `x`.
;; When we look at its type, we get `clojure.lang.Var$Unbound`.

(type x)

;; If we want to just look at the symbol itself, we can quote it.

(type (quote x))

;; The second argument is whatever data we wish to intern in that Var.

(def x 4)
x

;; Because Vars are mutable, we can redefine them later in our code.

(def x :four)
x

;; This behavior is similar behavior to Python variables and it can
;; subtly introduce errors while you are exploring your data.

;; Imagine you pull in a big set of real world data in desperate need
;; of tidying up and assign that data to a Var under the symbol `real-data`.

(def real-data [:big :old :gnarly :dataset])

;; Then you do all the hard work of removing the old and gnarly data
;; and assign it to the same Var...

(def real-data (remove #{:old :gnarly} real-data))

;; And then you remember, you had a function to update the old data...

(defn update-old->new  [data] (if (= :old data)
                          :new
                          data))


;; So you add this to your new `real-data` `def`.
;; By now, you might have an inkling of where this is going.

(def real-data
  (->> real-data
      (map update-old->new)
      (remove #{:gnarly})))

real-data

;; And the new data isn't there!

;; If you keep reassigning updated data to the same Var,
;  you lose the ability to go back and insert new transformations or correct old ones.
;; This can be complicated by the environment you are developing in.
;; Using notebooks and REPLs make working with data like this a joy.
;; You can test syntax and transformations before you commit to them.
;; You can dig deep into the data using all the power available to you.
;; But there are a few things you need to watch out for.

;; Let's take a look at the source code for this very file. The very first
;; Clojure code you see in this chapter is:

^:kindly/hide-code
'(def x)

;; But I wrote some hidden code before that:
^:kindly/hide-code
'(ns-unmap *ns* 'x)

;; `ns-unmap` takes a namespace and a symbol and removes the mapping
;; of that symbol from the namespace. `*ns*` is shorthand for the current
;; namespace. And the apostrophe before `x` is shorthand quoting the symbol
;; `x` and not what `x` resolves to.

;; I went through all this trouble because, once we started binding values
;; to the Var `x`, the code `(type x)` would no longer give me the same
;; results. `x` would be bound to whatever code was run last, and as I
;; worked on the file, reevaluating things, that could be the number `4`
;; or the keyword `:four` at various times.

;; For this reason, it is recommended that you choose new, more descriptive
;; symbols instead of reflexively assigning transformed data to an old Var.

;; ### A Couple Notes About Vars

;; #### Doc-strings

;; You can attach metadata to Vars, which in turn means you can give them docstrings.
;; If the second argument to a `def` is a string and there is a third argument,
;; then that string will become the doc-string.

(def real-data
  "Gathered from numberous sources, very few of them dubious."
  [:big :old :gnarly :dataset])

;; This not only provides you with a covenient place to remark on the data's origin
;; and the transformations it has been put through,
;; but it also attaches those remarks to the Var's metadata.
;; At any later point, you can retrieve it programmatically like so:

(-> (var real-data)
    meta
    :doc)

;; #### `defonce`
;; The macro `defonce` will work like `def`,
;; but only if the Var given to it doesn't already have a root value.
;; This is useful when you are defining a Var through some expensive calculation
;; or other process you don't want repeated every time you evaluate the namespace.

^:kindly/hide-code
(kind/hidden (ns-unmap *ns* 'so-much-data))
(defonce so-much-data [:that :takes :awhile :to :downloaded :from :some :url])

;; Note: `defonce` does not prevent a `def` from rebinding the Var.
;; It only prevents itself from redefining a Var that has already been defined.

so-much-data

;; And then..

(defonce so-much-data [:much :less :data])

so-much-data

;; ...changes nothing. However...

(def so-much-data [:changed :data])

so-much-data

;; ...will change your Var out from under on you.

;; ### The `let` Form

;; In idiomatic Clojure, `def` is typically only used at the top level,
;; for data that needs to be available globally.
;; When we're inside a function definition,
;; we use `let` to create lexically scoped bindings:

;; The second argument to a `let` expression is a vector of binding pairs.
;; In the simpliest form, these are symbols and the expressions you wish to bind to these symbols.
;; The rest of the `let` form is then evaluated with these symbols resolving
;; to the expressions they are paired with.
;; The `let` form itself evaluates to whatever the last form in its body evaluates to.

(let [x 4
      y "four"
      z :iv]
 {y x z x})

;; Inside this `let` we are binding the integer `4` to the symbol `x`,
;; the string `"string"` to the symbol `y`,
;; and the keyword `:iv` to the symbol `z`.
;; Then, in the body of the `let` expression we create a map literal
;; with the values of `x` and `z` as keys and the value of `y` as the values.

;; Outside of the `let` form, `x` retains the value we last defined it as.

x

;; It's important to note that we are not creating variables.
;; You cannot alter the values of these symbols in the body of the `let`
;; except by introducing another `let` or similar binding form.

;; However, inside the binding vector, the bindings are resolved in order,
;; and you can redefine the same symbol.
;; You can then use this to chain a series of transformations,
;; each built on and overshadowing the previous values.

(let [x :four      ; :four, a keyword
      x (name x)   ; "four", the name String of the keyword :four
      x (count x)] ; 4, the number of Characters in the String "four"
  x)

;; Because `let` keeps these definitions from leaking into the global context,
;; we will be using in our examples whenever we need to bind something
;; that we don't want to be available globally.

;; ##  Clojure Seqs, Lists and Vectors
(util/quote-python "
L = list(range(10))
L
[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
")

(util/quote-python "
type(L[0])
")

(util/quote-python "
L2 = [str(c) for c in L]
L2
['0', '1', '2', '3', '4', '5', '6', '7', '8', '9']
")

(let [l0 (range 10)
      l1 (map str l0)]
  {:l0 l0
   :type-in-l0 (type (first l0))
   :l1 l1
   :type-in-l1 (type (first l1))})


(util/quote-python "
L3 = [True, \"2\", 3.0, 4]
[type(item) for item in L3]
[bool, str, float, int]
")

(map type [true "2" 3.0 4])

(util/quote-python "
import array
L = list(range(10))
A = array.array('i', L)
A
array('i', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9])
")

;; ## Fixed-Type Arrays in Clojure

;; Maybe talk a bit about Java's relation to Clojure here?

(let [L (range 10)
      A (int-array L)]
  A)

;; ## Creating Arrays and Buffers from Clojure Seqs, Lists and Vectors

(util/quote-python "
import numpy as np
# integer array:
np.array([1, 4, 2, 5, 3])
")

;; Using the Clojure core function `to-array`, we can convert any Clojure
;; collection into a Java array.

(let [arr (to-array [1 4 2 5 3])]
  [(type arr) arr])

;; This notation `[Ljava.lang.Object;` with the open bracket at the beginning,
;; but no closing bracket is the JVM's way of saying, "An array of the following type."
;; What follows the `[` indicates the types in the array.
;; In this case, the `Ljava.lang.Object;` means this is just a an array of objects.

^:kindly/hide-code
(kind/hiccup
 [:table
  [:thead
   [:tr [:th "Element Type"] [:th "Encoding"]]]
  [:tbody
   [:tr [:td "boolean"] [:td "Z"]]
   [:tr [:td "byte"] [:td "B"]]
   [:tr [:td "char"] [:td "C"]]
   [:tr [:td "class or interface"] [:td "L" [:em "classname"] ";"]]
   [:tr [:td "double"] [:td "D"]]
   [:tr [:td "float"] [:td "F"]]
   [:tr [:td "int"] [:td "I"]]
   [:tr [:td "long"] [:td "J"]]
   [:tr [:td "short"] [:td "S"]]]])

;; [table source](https://docs.oracle.com/javase/9/docs/api/java/lang/Class.html#getName--)

;; So `to-array` returns an array of objects, but we can do better.

(let [arr (int-array [1 4 2 5 3])]
  [(type arr) arr])

;; Here the notation `[I` means we've returned a Java array of ints.
;; This is better.
;; But a Java array is not the same thing as a Numpy array.
;; There is no guarantee that a Java array will be stored in contiguous
;; memory locations.

;; ### `dtype-next` and the `buffer` interface

;;

(util/quote-python "
np.array([3.14, 4, 2, 3])
array([ 3.14,  4.  ,  2.  ,  3.  ])
")

(util/quote-python "
np.array([1, 2, 3, 4], dtype='float32')
array([ 1.,  2.,  3.,  4.], dtype=float32)
")

(let [buf (dtype/->reader [3.14 4 2 5 3])]
  [(dt/elemwise-datatype buf) buf])

;; Unlike Numpy, `dtype` won't automatically infer the datatype.
;; It is better to be explicit.

;; You can provide `dtype/->reader` with a keyword argument indicating
;; the type you want:

(let [buf (dtype/->reader [3.14 4 2 5 3] :float64)]
  [(dt/elemwise-datatype buf) buf])

(util/quote-python "
# nested lists result in multi-dimensional arrays
np.array([range(i, i + 3) for i in [2, 4, 6]])
array([[2, 3, 4],
       [4, 5, 6],
       [6, 7, 8]])
")

;; We will use `tech.v3.tensor` as `dtt` to create n-dimensional buffers.
;; Here we indicate the data type in an options map at the end of the
;; arguments list.

(-> (map (fn [n] (range n (+ n 3))) [2 4 6])
    (dtt/->tensor {:datatype :int}))

;; ## Creating Arrays from Scratch

(util/quote-python "
# Create a length-10 integer array filled with zeros
np.zeros(10, dtype=int)
array([0, 0, 0, 0, 0, 0, 0, 0, 0, 0])
")

(let [buf (dtype/const-reader 0 10)]
  [(dtype/elemwise-datatype buf) buf])

(util/quote-python "
# Create a 3x5 floating-point array filled with ones
np.ones((3, 5), dtype=float)
array([[ 1.,  1.,  1.,  1.,  1.],
       [ 1.,  1.,  1.,  1.,  1.],
       [ 1.,  1.,  1.,  1.,  1.]])
")

(dtt/const-tensor 1.0 [3 5])

(util/quote-python "
# Create a 3x5 array filled with 3.14
np.full((3, 5), 3.14)
array([[ 3.14,  3.14,  3.14,  3.14,  3.14],
       [ 3.14,  3.14,  3.14,  3.14,  3.14],
       [ 3.14,  3.14,  3.14,  3.14,  3.14]])
")

(dtt/const-tensor 3.14 [3 5])

(util/quote-python "
# Create an array filled with a linear sequence
# Starting at 0, ending at 20, stepping by 2
# (this is similar to the built-in range() function)
np.arange(0, 20, 2)
array([ 0,  2,  4,  6,  8, 10, 12, 14, 16, 18])
")

(dtype/make-reader :int 10 (* idx 2))

(util/quote-python "
# Create an array of five values evenly spaced between 0 and 1
np.linspace(0, 1, 5)
array([ 0.  ,  0.25,  0.5 ,  0.75,  1.  ])
")

;; Obviously not all `numpy` or `pandas` code will have a `Clojure`
;; doppleganger. But it is not terribly difficult to compose your own.

;; To recreate `np.linspace` we'll create a range of numbers, from 0 (inclusive)
;; to the number of steps requested (exclusive).
;; Then we divide each number by one less then the nubmer of steps and
;; multiply it by a factor equal to the ending number minus the starting number.
;; Finally, we add the starting number to each result.

(defn linear-space [begin end steps]
  (dtype/make-reader :float steps
                     (+ begin
                        (* idx
                           (/ (- end begin)
                              (dec steps))))))

(linear-space 0 1 5)

(util/quote-python "
# Create a 3x3 array of uniformly distributed
# random values between 0 and 1
np.random.random((3, 3))
array([[ 0.99844933,  0.52183819,  0.22421193],
       [ 0.08007488,  0.45429293,  0.20941444],
       [ 0.14360941,  0.96910973,  0.946117  ]])
")

;; Here again, there is no convenience function in `dtype` or `tensor`,
;  but it is not hard to fashion our own:

(dtt/compute-tensor [3 3] (fn [& _] (rand)) :float)

(util/quote-python "
# Create a 3x3 array of normally distributed random values
# with mean 0 and standard deviation 1
np.random.normal(0, 1, (3, 3))
array([[ 1.51772646,  0.39614948, -0.10634696],
       [ 0.25671348,  0.00732722,  0.37783601],
       [ 0.68446945,  0.15926039, -0.70744073]])
")

;; Okay, this will take some doing. The naive approach is to grab a random
;; float in a reasonable range...

(defn rand-float-in-range
  [low high]
  (+ low (* (- high low) (rand))))

;; ...apply the probablity density function for the normal distribution
;; to that random float...

(defn normal-pdf
  [mean sdev x]
  (let [multiplier (/ (math/sqrt (* 2 math/PI sdev sdev)))
        exponent (/ (- (math/pow (- x mean) 2))
                    (* 2 sdev sdev))]
    (* multiplier (math/exp exponent))))

;; ...then use that result to randomly determine if we accept or
;; reject that value. If we reject it, we start all over until
;; we find an acceptable result.


(defn rand-dist [dist low high]
  (let [in-dist? (fn [x] (when (<= (rand) (dist x)) x))]
    (loop [x (in-dist? (rand-float-in-range -4 4))]
      (if x x (recur (in-dist? (rand-float-in-range -4 4)))))))

(defn rand-normal [mean sdev]
  (let [dist (partial normal-pdf mean sdev)]
    (rand-dist dist (- mean 4) (+ mean 4))))

(dtt/compute-tensor [3 3] (fn [& _] (rand-normal 0 1)) :float64)

(util/quote-python "
# Create a 3x3 array of random integers in the interval [0, 10)
np.random.randint(0, 10, (3, 3))
array([[2, 3, 4],
       [5, 7, 8],
       [0, 5, 0]])
")

(dtt/compute-tensor [3 3] (fn [& _] (rand-int 10)) :int64)

^:kindly/hide-code
(comment

  (dtt/compute-tensor [3 3]
                      (fn [& args] (if (apply = args) 1 0))
                      :int64)

  ;; What's going on with the datatype?
  (map #(map type %) (dtt/compute-tensor [3 3]
                                         (fn [& args] (if (apply = args) 1 0))
                                         :int))
  (dtt/compute-tensor [3 3]
                      (fn [& args] (if (apply = args) 1 0))
                      :int)
  (dtt/compute-tensor [3 3]
                      (fn [& args] (if (apply = args) 1 0))
                      :nonsense)
  (map #(map type %) (dtt/compute-tensor [3 3]
                                         (fn [& args] (if (apply = args) 1 0))
                                         :nonsense))
  (dtt/compute-tensor [3 3]
                      (fn [& args] (if (apply = args) 1 0))
                      :float)
;; So the datatype keyword has an effect on the actual type,
;; but that can be overwritten if it doesn't make sense
;; and the type shown by the printed tensor is just based on
;; that keyword and not the actual type.
)

(util/quote-python "
# Create a 3x3 identity matrix
np.eye(3)
array([[ 1.,  0.,  0.],
       [ 0.,  1.,  0.],
       [ 0.,  0.,  1.]])
")

;; Using `dtt/compute-tensor` we can construct an identity matrix
;; by checking placing a 1 wherever all the indices are equal
;; and a 0 wherever they are not.

(dtt/compute-tensor [3 3]
                      (fn [& args] (if (apply = args) 1 0))
                      :int64)

(util/quote-python "
# Create an uninitialized array of three integers
# The values will be whatever happens to already exist at that memory location
np.empty(3)
array([ 1.,  1.,  1.]) ")

(dtt/native-tensor [3] :int64)


; # Bottom
