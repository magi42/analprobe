package org.vaadin.multiprobe.data

import scala.collection.mutable.LinkedList

class ListWindow[T](val list: LinkedList[T]) {
  var first: LinkedList[T] = _
  var last: LinkedList[T] = _

  /**
   * Initializes the window to the first element range
   * that satisfies the given condition.
   */
  def init(selector: T => Boolean) {
    first = list
    while (first.elem != null && !selector(first.elem))
      first = first.next
    var nextToLast = first
    last = nextToLast
    while (nextToLast.elem != null && selector(nextToLast.elem)) {
      last = nextToLast
      nextToLast = nextToLast.next
    }
    
    if (first.elem == null)
      first = null
    if (last.elem == null)
      last = null
  }

  def clear = {
    first = null
    last = null
  }
}

class DiscreteWindow[T](val window: ListWindow[T], val slots: Int, val value: T => Double) {
  val values: Array[Double] = new Array[Double](slots)
  
  def update() {
    
  }
}
