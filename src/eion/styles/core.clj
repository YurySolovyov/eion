(ns eion.styles.core
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule at-font-face]]))


(defstyles base
  (at-font-face {
    :font-family "OpenSans"
    :src "url('fonts/OpenSans-Regular.ttf')"
  })

  ["::-webkit-scrollbar" {
    :width "8px"
  }]

  ["::-webkit-scrollbar-thumb" {
    :background-color "#ccc"
    :background-clip "content-box"
    :border "2px transparent solid"
    :border-radius "4px"
  }
    [:&:hover :&:active {
      :background-color "#2196F3"
      :border-color "#2196F3"
      :border-radius "0"
    }]
  ]

  [:html {
    :height "100%"
  }]

  [:body {
    :height "100%"
    :font-family "OpenSans"
    :font-size "16px"
  }]

  [:.panel {
    :max-height "100vh"
    :flex "1"
  }]

  [:.panel-container {
    :flex-direction "column"
    :flex "1"
    :box-shadow "#999 0 1px 2px"
  }]

  [:.directory-list {
    :overflow-y "scroll"
    :overflow-x "hidden"
    :max-height "calc(100vh - 62px)"
    :flex-direction "column"
  }]

  [:.directory-item {
    :line-height "3rem"
    :min-height "3rem"
    :cursor "default"
    :-webkit-user-select "none"
    :font-size "0.875em"
    :border-bottom "1px rgba(0, 0, 0, 0.05)"
    :transition "0.2s"
  }
    [:.directory-item-type {
      :width "24px"
      :color "#234154"
    }]

    [:.directory-item-name {
      :flex "1"
    }]
  ]
)
