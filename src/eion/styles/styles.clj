(ns eion.styles.styles
  (:require [garden.def :refer :all]
            [garden.selectors :as s]
            [garden.stylesheet :refer [rule at-font-face]]))

(def high-row {
  :line-height "3.5rem"
  :flex-direction "row"
  :font-size "0.875em"
})

(def scroll-background "#ccc")
(def body-background "#fff")
(def theme-blue "#4dadf7")
(def theme-red "#fa5252")
(def theme-white "#e0e0e0")
(def theme-full-white "#fff")
(def theme-text-color "#34495e")
(def theme-icon-color "#34495e")

(def shadow-for-active (str theme-blue " 0 1px 5px"))
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
  }
    [:&.active
      [:.panel-container {
        :outline (str "1px " theme-blue " solid")
        :box-shadow shadow-for-active
      }]
    ]
  ]

  [:.panel-container {
    :flex-direction "column"
    :flex "1"
    :box-shadow shadow-strong
    :max-width "calc(50vw - 20px)"
    :transition "0.2s"
    :outline (str "1px transparent solid")
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
      :transition "0.2s"
    }
      [:&:hover {
        :color theme-blue
      }]

      [:.location-highlight {
        :background-color "transparent"
        :height "2px"
        :transition "0.2s"
      }]

      [:&.current {
        :color theme-blue
      }
        [:.location-highlight {
          :background-color theme-blue
        }]
      ]
    ]
  ]

  [:.directory-path {
    :flex-direction "row"
  }
    [:.panel-path {
      :font-family "OpenSans"
      :font-size "1em"
      :flex "1"
      :border "none"
      :background-color "transparent"
      :text-overflow "ellipsis"
      :white-space "nowrap"
      :overflow "hidden"
      :transition "0.2s"
    }
      [:&:focus {
        :outline "none"
        :color theme-blue
      }]

      [:&.error {
        :color theme-red
      }]
    ]

    [:.up-button {
      :border-radius "2px"
      :transition "0.2s"
      :cursor "pointer"
    }
      [:&:hover {
        :color theme-full-white
        :background-color theme-blue
      }]
    ]
  ]

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

  [:.directory-progress {
    :width "0%"
    :height "2px"
    :background-color theme-blue
    :margin-top "-2px"
    :transition "opacity 0.4s"
  }
    [:&.full {
      :opacity 0
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
    :max-height "calc(100vh - 270px)"
    :min-height "calc(100vh - 270px)"
    :flex-direction "column"
  }]

  [:.directory-item {
    :line-height "3rem"
    :min-height "3rem"
    :height "3rem"
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
      :background-color theme-blue
      :color theme-full-white
    }]

    [:.directory-item-type {
      :width "32px"
      :color theme-icon-color
      :line-height "4em"
    }
      [:&.file {
        :justify-content "center"
        :flex-direction "column"
      }]
    ]

    [:.directory-item-name {
      :flex "3"
      :text-overflow "ellipsis"
      :word-wrap "break-word"
      :white-space "nowrap"
      :overflow "hidden"
    }]

    [:.directory-item-name-field {
      :flex "3"
    }

      [:input {
        :line-height "44px"
        :border "none"
        :font-family "OpenSans"
        :font-size "1em"
        :color theme-text-color
        :width "100%"
        :transition "0.2s"
      }
        [:&:focus {
          :outline "none"
          :color theme-blue
        }]

        [:&.error {
          :color theme-red
        }]
      ]
    ]

    [:.directory-item-meta {
      :flex "1"
    }
      [:.directory-item-ext :.directory-item-size {
        :flex "1"
        :text-align "right"
      }]
    ]
  ]

  [:#actions {
    :height "48px"
    :justify-content :center
  }
    [:.file-buttons {
      :cursor :pointer
      :color theme-text-color
      :align-items :center
      :flex-direction :row
      :box-shadow shadow-strong
    }
      [:.file-button {
        :transition "0.2s"
        :min-width "100px"
        :text-align :center
      }
        [:&:hover {
          :color theme-blue
        }]
      ]
    ]
  ]

  [:.dialog-wrapper {
    :display "none"
    :position "absolute"
    :top 0
    :right 0
    :bottom 0
    :left 0
    :margin "auto"
    :width "500px"
    :height "500px"
    :background-color theme-full-white
    :box-shadow shadow-strong
    :outline (str "1px transparent solid")
  }

    [:&.active {
      :display "block"
      :outline (str "1px " theme-blue " solid")
    }]
  ]
)
