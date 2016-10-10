(ns eion.styles.core
  (:require [garden.def :refer :all]
            [garden.selectors :as s]
            [garden.stylesheet :refer [rule at-font-face]]))

(def high-row {
  :line-height "3.5rem"
  :flex-direction "row"
  :font-size "0.875em"
})

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
    :color "#212121"
  }]

  [:.panel {
    :max-height "100vh"
    :flex "1"
  }]

  [:.panel-container {
    :flex-direction "column"
    :flex "1"
    :box-shadow "#999 0 1px 2px"
    :max-width "calc(50vw - 20px)"
  }]

  [:.locations {
    :border-bottom "1px #E0E0E0 solid"
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
        :color "#2196F3"
        :border-color "#2196F3"
      }]
    ]
  ]

  [:.directory-path {
    :flex-direction "row"
  }]

  [:.directory-list-header {
    :flex-direction "column"
    :box-shadow "#ccc 0 1px 1px"
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
    :border-top "1px #E0E0E0 solid"
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
    :border-bottom "1px #E0E0E0 solid"
    :transition "0.2s"
  }
    [:&:last-child {
      :border-color "transparent"
    }]

    [:&:first-child {
      :border-color "#E0E0E0"
    }]

    [:&.selected {
      :background-color "#E0E0E0"
    }]

    [:.directory-item-type {
      :width "24px"
      :color "#234154"
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
