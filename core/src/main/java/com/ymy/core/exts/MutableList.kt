package com.ymy.core.exts

/**
 * Created on 2020/8/27 16:58.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
inline fun <T> MutableList<T>.mapInPlace(mutator: (T)->T) {
    val iterate = this.listIterator()
    while (iterate.hasNext()) {
        val oldValue = iterate.next()
        val newValue = mutator(oldValue)
        if (newValue !== oldValue) {
            iterate.set(newValue)
        }
    }
}