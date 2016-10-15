(ns eion.styles.core
  (:require [garden.def :refer :all]
            [garden.selectors :as s]
            [garden.stylesheet :refer [rule at-font-face]]))

(def high-row {
  :line-height "3.5rem"
  :flex-direction "row"
  :font-size "0.875em"
})

(def scroll-background "#ccc")
(def body-background "#FFF")
(def theme-blue "#4dadf7")
(def theme-white "#E0E0E0")
(def theme-text-color "#34495e")
(def theme-icon-color "#34495e")

(def shadow-strong "#999 0 1px 2px")
(def shadow-light "#ccc 0 1px 1px")

(defstyles base
  (at-font-face {
    :font-family "OpenSans"
    :src "url('fonts/OpenSans-Regular.ttf')"
  })

  ["::-webkit-scrollbar" {
    :width "8px"
    :height "8px"
  }]

  ["::-webkit-scrollbar-thumb" {
    :background-color scroll-background
    :background-clip "content-box"
    :border "2px transparent solid"
    :border-radius "4px"
  }
    [:&:hover :&:active {
      :background-color theme-blue
      :border-color theme-blue
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
    :color theme-text-color
    :background-color body-background
  }]

  [:.panel {
    :max-height "100vh"
    :flex "1"
  }]

  [:.panel-container {
    :flex-direction "column"
    :flex "1"
    :box-shadow shadow-strong
    :max-width "calc(50vw - 20px)"
  }]

  [:.locations {
    :border-bottom (str "1px " theme-white " solid")
    :line-height "3em"
    :overflow "hidden"
  }
    [:.locations-list {
      :transition "margin 0.2s"
    }]

    [:.location {
      :cursor "pointer"
      :padding "0 0.75em"
      :min-width "8em"
      :text-align "center"
      :border-bottom "2px transparent solid"
    }
      [:&.current {
        :color theme-blue
        :border-color theme-blue
      }]
    ]
  ]

  [:.directory-path {
    :flex-direction "row"
  }]

  [:.directory-list-header {
    :flex-direction "column"
    :box-shadow shadow-light
  }]

  [:.directory-header high-row
    [:.directory-header-name {
      :flex "2"
    }]

    [:.directory-header-ext :.directory-header-size {
      :flex "0.3"
      :justify-content "flex-end";
    }]
  ]

  [:.directory-list-footer (merge high-row {
    :border-top (str "1px " theme-white " solid")
  })
    [:.directory-summary {
      :flex "1"
      :justify-content "space-between"
    }]
  ]

  [:.directory-list {
    :overflow-y "scroll"
    :overflow-x "hidden"
    :max-height "calc(100vh - 231px)"
    :min-height "calc(100vh - 231px)"
    :flex-direction "column"
  }]

  [:.directory-item {
    :line-height "3rem"
    :min-height "3rem"
    :cursor "default"
    :-webkit-user-select "none"
    :font-size "0.875em"
    :border-bottom (str "1px " theme-white " solid")
    :transition "0.2s"
  }
    [:&:last-child {
      :border-color "transparent"
    }]

    [:&:first-child {
      :border-color theme-white
    }]

    [:&.selected {
      :background-color theme-white
    }]

    [:.directory-item-type {
      :width "24px"
      :color theme-icon-color
      :line-height "4em"
    }]

    [:.directory-item-name {
      :flex "3"
      :text-overflow "ellipsis"
      :word-wrap "break-word"
      :white-space "nowrap"
      :overflow "hidden"
    }]

    [:.directory-item-meta {
      :flex "1"
    }
      [:.directory-item-ext :.directory-item-size {
        :flex "1"
        :text-align "right"
      }]
    ]
  ]
)
