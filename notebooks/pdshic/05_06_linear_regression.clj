;; # In Depth: Linear Regression

;; [original chapter](https://jakevdp.github.io/PythonDataScienceHandbook/05.06-linear-regression.html)

(ns pdshic.05-06-linear-regression
  (:require [scicloj.kindly.v4.kind :as kind]
            [pdshic.util :as util]
            [tablecloth.api :as tc]
            [fastmath.random :as random]
            [scicloj.noj.v1.vis.hanami :as hanami]
            [aerial.hanami.templates :as ht]
            [tech.v3.datatype :as dtype]
            [tech.v3.datatype.functional :as fun]))

;; ## Simple Linear Regression

;; Let us create some random data going approximately along a line.
(util/quote-python "
rng = np.random.RandomState(1)
x = 10 * rng.rand(50)
y = 2 * x - 5 + rng.randn(50)
plt.scatter(x, y);
")

(def rng
  (random/rng 1))

;; We use efficient array-like processing through `dtype-next`,
;; but of course we could also use plain `map` calls for the arithmetic,
;; and it would be just fine for this size of data.

(def x
  (-> (dtype/make-reader :float32 50 (random/drandom rng))
      (fun/* 10)
      dtype/clone))

(def y
  (-> x
      (fun/* 2)
      (fun/- 5)
      (fun/+ (dtype/make-reader :float32 50 (random/grandom rng)))
      dtype/clone))

(def scatter
  (-> {:x x :y y}
      tc/dataset
      (hanami/plot ht/point-chart {})))

scatter

;; Let us find an optimal line predicting `y` by `x` ("linear regression").

(util/quote-python "
from sklearn.linear_model import LinearRegression
model = LinearRegression(fit_intercept=True)

model.fit(x[:, np.newaxis], y)

xfit = np.linspace(0, 10, 1000)
yfit = model.predict(xfit[:, np.newaxis])

plt.scatter(x, y)
plt.plot(xfit, yfit);
")

(def model
  (fun/linear-regressor x y))

;; Note: the `layers` function we are using here is still experimental.

(let [xfit (range 0 10 1/100)
      yfit (map model xfit)]
  (hanami/layers nil
                 {}
                 [scatter
                  (-> {:x xfit
                       :y yfit}
                      tc/dataset
                      (hanami/plot ht/line-chart {:MCOLOR "brown"}))]))
