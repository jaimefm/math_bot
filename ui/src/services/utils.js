import elementResizeEvent from 'element-resize-event'
import _ from 'underscore'

const utils = {
  findStepInd (steps, step) {
    return steps.reduce((ind, s, i) => {
      if (s.name === step) {
        ind = i
      }
      return ind
    }, 0)
  },
  openPopover (targetId) {
    this.$root.$emit('bv::show::popover', targetId)
  },
  closePopover (targetId) {
    this.$root.$emit('bv::hide::popover', targetId)
  },
  assembleLevels (levels) {
    const assembleLevels = _.chain(levels)
      .map((l, k) => {
        return [k, {
          level: l,
          name: k,
          prevStep: _.find(l, v => v.prevStep === 'None').prevLevel,
          nextStep: _.find(l, v => v.nextStep === 'None').nextLevel
        }]
      })
      .object()
      .value()

    return this.orderEm(assembleLevels)
  },

  orderEm (steps) {
    const stepsInOrder = Object.keys(steps).filter(key => steps[key].prevStep === 'None').map(s => steps[s]);

    (function ordEm () {
      if (stepsInOrder[stepsInOrder.length - 1].nextStep === 'None') return
      stepsInOrder.push(steps[stepsInOrder[stepsInOrder.length - 1].nextStep])

      ordEm()
    })()

    return stepsInOrder
  },

  parseCamelCase (str) {
    return str.split('')
      .map(l => l === l.toUpperCase() ? ` ${l}` : l).join('')
  },

  /*
    watcher is a watcher function that uses trampolining to watch a value in the store to change. example use case is to watch for an item to be added to a function in order to trigger the next step of the tutorial.

    testFunction is the blocking condition for the trampoline.
  **/
  watcher (testFunction, cb) {
    const trampoline2 = () => {
      setTimeout(() => {
        trampoline1()
      },
      100
      )
    }

    const trampoline1 = () => {
      let count = 0

      while (testFunction()) {
        count++
        if (count > 100) {
          trampoline2()
          return
        }
      }
      cb()
    }

    trampoline1()
  },
  /*
    scroll is used to make any scrollable element scrollable.
  **/
  scroll (evt, dir, element) {
    evt.preventDefault()
    const step = dir === 'up' ? -100 : 100
    $('.' + element).animate({
      scrollTop: '+=' + step + 'px'
    })
  },
  /*
    watchForElementResize watches the passed in function for resize then calls the callback function.
  **/
  watchForElementResize (ele, cb) {
    elementResizeEvent(ele, () => {
      cb()
    })
  },
  /*
    convertPxToEms converts the passed in width and height to em;
  **/
  convertPxToEms (width, height) {
    const body = document.body
    const bodyFontSize = window.getComputedStyle(body, null).getPropertyValue('font-size').split('p')[0]
    const eleHeight = height / Number(bodyFontSize)
    const eleWidth = width / Number(bodyFontSize)

    return {
      height: eleHeight,
      width: eleWidth
    }
  },
  /*
    setZ sets the z-index of the passed in element by the passed in amount.
  **/
  setZ (element, howMuch) {
    $(element).css({
      'z-index': howMuch
    })
  },
  /*
  setOpacity sets the opacity of the passed in element by the passed in amount.
  **/
  setOpacity (element, howMuch) {
    $(element).css({
      'opacity': howMuch
    })
  },
  /*
    changeElementBackground changes the background to passed in string.
  **/
  changeElementBackGround (ele, background) {
    $(ele).css({background: background})
  },
  removeStyle (ele, style) {
    $(ele).css(style, '')
  },
  /*
    below are animations.
  **/
  doIt (element, animation) {
    $(element).addClass(animation)
    setTimeout(() => {
      $(element).removeClass(animation)
    },
    1000
    )
  },
  animateFlipInX (element, cb) {
    this.doIt(element, 'flipInX')
    if (cb !== undefined) {
      cb()
    }
  },
  animateFlipOutX (element, cb) {
    this.doIt(element, 'flipOutX')
    if (cb !== undefined) {
      cb()
    }
  },
  animateTada (element, cb) {
    this.doIt(element, 'tada')
    if (cb !== undefined) {
      cb()
    }
  },
  animateRotate (element, cb) {
    this.doIt(element, 'rotateIn')
    if (cb !== undefined) {
      cb()
    }
  }
}

export default utils
