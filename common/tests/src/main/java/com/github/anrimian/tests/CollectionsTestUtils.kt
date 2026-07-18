package com.github.anrimian.tests

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

object CollectionsTestUtils {

    fun <K, M> Map<K, M>.assertContainsSingleKey(key: K) {
        assertEquals(1, this.size)
        assertTrue(this.containsKey(key))
    }

    fun <K, M> Map<K, M>.assertContainsSingleEntry(key: K, value: M) {
        assertContainsSingleEntry(key to value)
    }

    fun <K, M> Map<K, M>.assertContainsSingleEntry(entry: Pair<K, M>) {
        assertContainsExactEntries(entry)
    }

    fun <K, E> Map<K, E>.assertEmpty(name: String) {
        assertContainsExactEntries(name = name)
    }

    fun <K, M> Map<K, M>.assertContainsExactEntries(vararg entries: Pair<K, M>, name: String = "") {
        val removeMap = HashMap(this)
        entries.forEach { (key, value) ->
            assertTrue(this.containsKey(key), "Map $name doesn't contains key: $key, but contains: $this")
            assertTrue(this[key] == value, "Value for '$key' is: '${this[key]}' expected: '$value', actual values: $this")
            removeMap.remove(key)
        }
        removeMap.forEach { (key, value) ->
            throw AssertionError("Map $name contains unwanted entry: [$key]=$value")
        }
    }


    fun <M> List<M>.assertContains(item: M) {
        assertTrue(this.contains(item))
        assertEquals(1, this.size)
    }

    fun <M> List<M>.assertContainsSingleItem(item: M) {
        assertContainsExactValues(item)
    }

    fun <E> Collection<E>.assertEmpty(name: String) {
        assertContainsExactValues(name = name)
    }

    fun <K> Collection<K>.assertContainsExactValues(
        vararg values: K,
        name: String = "",
        checkIndex: Boolean = false
    ) {
        val removeList = ArrayList(this)
        values.forEachIndexed { i, value ->
            if (checkIndex) {
                assertTrue(this.elementAt(i) == value, "Collection $name doesn't contains value: $value at index: $i \n But contains value: ${this.elementAt(i)}")
            } else {
                assertTrue(this.contains(value), "Collection $name doesn't contains value: $value \n But contains values: $this")
            }
            removeList.remove(value)
        }
        if (removeList.isNotEmpty()) {
            throw AssertionError("Collection $name contains unwanted entries: ${removeList.joinToString()}}")
        }
    }

    fun <K> Set<K>.assertContainsSingleValue(key: K) {
        assertContainsExactValues(key)
    }

    fun <K> Set<K>.assertContainsExactValues(keys: Set<K>) {
        assertEquals(keys.size, this.size)
        assertTrue(this.containsAll(keys))
    }

    fun <K> Set<K>.assertContainsExactValues(vararg values: K) {
        val removeSet = HashSet(this)
        values.forEach { value ->
            assertTrue(this.contains(value), "Set doesn't contains value: $value \nBut contains values: $this")
            removeSet.remove(value)
        }
        removeSet.forEach { value ->
            throw AssertionError("Set contains unwanted entry: $value")
        }
    }

}