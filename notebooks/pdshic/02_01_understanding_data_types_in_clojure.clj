;; # Understanding Data Types in Clojure

;; [original chapter](https://jakevdp.github.io/PythonDataScienceHandbook/02.01-understanding-data-types.html)

(ns pdshic.02-01-understanding-data-types-in-clojure
  (:require [pdshic.util :as util]
            [scicloj.kindly.v4.kind :as kind]
            [tablecloth.api :as tc]
            [tech.v3.datatype :as dtype]
            [tech.v3.tensor :as dtt]
            [tech.v3.datatype.functional :as fun]))

;; Like Python, Clojure enjoys the benefits of dynamic typing. In Python any data
;; can be assigned to any variable:

(util/quote-python "
x = 4
x = \"four\"
")

;; Clojure Vars are akin to Python variables. When you use the `def` special form
;; you create a Var and [intern](https://clojure.org/reference/vars#interning)
;; it under the provided symbol.

(def x)

;; Here we've created an unbound Var and assigned it to the symbol `x`

(type x)

(type 'x)

;; We can assign whatever data we wish to the Var.

(def x 4)
x

(def x :four)
x

;; But `def` is typically used at the top level, for data that needs to be
;; available globally. When we're inside a function definition, typically
;; we will use a binding like with `let`:

(let [x 4
      x :four
      x "four"]
  x)


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
      type-in-l0 (type (first l0))
      l1 (map str l0)
      type-in-l1 (type (first l1))]
  [type-in-l0 type-in-l1])


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

(let [L (range 10)
      A (int-array L)]
  A)

;; ## Creating Arrays from Clojure Lists and Vectors

(util/quote-python "
import numpy as np
# integer array:
np.array([1, 4, 2, 5, 3])
")

(let [arr (dtype/->array [1 4 2 5 3])]
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

;; Anytime we don't explicitly give it a datatype, it will be considered an array
;; of objects. It is more useful to provide a type:

(let [arr (dtype/->array :int [1 4 2 5 3])]
  [(type arr) arr])

;; Or...

(let [arr (dtype/->int-array [1 4 2 5 3])]
  [(type arr) arr])


(util/quote-python "
np.array([3.14, 4, 2, 3])
array([ 3.14,  4.  ,  2.  ,  3.  ])
")

(let [arr (dtype/->array [3.14 4 2 5 3])]
  [(type arr) arr])

;; Unlike Numpy, `dtype` won't automatically infer the data type. It is better
;; to be explicit.

(util/quote-python "
np.array([1, 2, 3, 4], dtype='float32')
array([ 1.,  2.,  3.,  4.], dtype=float32)
")

(let [arr (dtype/->array :float [3.14 4 2 5 3])]
  [(type arr) arr])

(util/quote-python "
# nested lists result in multi-dimensional arrays
np.array([range(i, i + 3) for i in [2, 4, 6]])
array([[2, 3, 4],
       [4, 5, 6],
       [6, 7, 8]])
")

;; We will use `tech.v3.tensor` as `dtt` to create multidimensional arrays.
;; Here we indicate the data type in an options map at the end of the
;; arguments list.

(-> (map (fn [n] (range n (+ n 3))) [2 4 6])
    (dtt/->tensor {:datatype :int}))
