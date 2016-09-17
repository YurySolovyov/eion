(ns eion.styles.core
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule at-font-face]]))


(defstyles base
  (at-font-face {
    :font-family "OpenSans"
    :src "url('OpenSans-Regular.ttf')"
  })

  [:html {
    :height "100%"
  }]

  [:body {
    :font-family "OpenSans"
    :font-size   "16px"
    :line-height 1.5
  }]

  [:.panel {
    :height "100vh"
    :width "50vw"
    :flex-direction "column"
  }]

  [:.directory-list {
    :overflow-y "scroll"
    :overflow-x "hidden"
    :height "100%"
  }]

  [:.directory-item {
    :line-height "24px"
    :cursor "default"
    :-webkit-user-select "none"
    :font-size "0.875rem"
    :border-bottom "1px rgba(0, 0, 0, 0.05)"
    :transition "0.2s"
  }]
)
