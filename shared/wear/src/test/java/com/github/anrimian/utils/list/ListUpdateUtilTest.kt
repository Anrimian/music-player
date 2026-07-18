package com.github.anrimian.utils.list

import com.github.anrimian.utils.list.ListUpdateUtilTest.TestPair
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class ListUpdateUtilTest {

    companion object {
        @JvmStatic
        fun generateTestData(): Stream<Arguments> {
            return Stream.of(
                //shift forward
                Arguments.of(listOf("1", "2", "3", "4"), listOf("2", "3", "4", "5")),
                //shift forward by 2
                Arguments.of(listOf("1", "2", "3", "4"), listOf("3", "4", "5", "6")),
                //shift backward
                Arguments.of(listOf("1", "2", "3", "4"), listOf("0", "1", "2", "3")),
                //shift backward by 2
                Arguments.of(listOf("1", "2", "3", "4"), listOf("-1","0", "1", "2")),
                //complete new
                Arguments.of(listOf("1", "2", "3", "4"), listOf("6", "7", "8", "9")),
                //almost new
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "3", "4", "5")),
                Arguments.of(
                    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                    listOf("10", "11", "1", "12", "13", "14", "3", "15", "16")
                ),
                //add to end
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "2", "3", "4", "5")),
                //add to start
                Arguments.of(listOf("1", "2", "3", "4"), listOf("0", "1", "2", "3", "4")),
                //add to middle
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "2", "22", "3", "4")),
                //add to middle, multiple
                Arguments.of(listOf("1", "3"), listOf("0", "1", "2", "3")),
                //remove from end
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "2", "3")),
                //remove from start
                Arguments.of(listOf("1", "2", "3", "4"), listOf("2", "3", "4")),
                //remove from middle
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "3", "4")),
                //clear all
                Arguments.of(listOf("1", "2", "3", "4"), listOf<String>()),

                //move items
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "3", "2", "4")),
                //move items + shift
                Arguments.of(listOf("1", "2", "3", "4"), listOf("0", "1", "3", "2", "4")),
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "1.1", "3", "2", "4")),
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "3", "2", "3.5", "4")),
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "3", "3.5", "3.6", "2", "4")),
                Arguments.of(listOf("1", "2", "3", "4"), listOf("1", "3", "2", "4", "5")),
            )
        }

        @JvmStatic
        fun generateModifyItemTestData(): Stream<Arguments> {
            return Stream.of(
                //simple modify
                Arguments.of(
                    listOf("1" to "1", "2" to "2", "3" to "3"),
                    listOf("1" to "1", "2" to "2.5", "3" to "3")
                ),
                //modify+add before
                Arguments.of(
                    listOf("1" to "1", "2" to "2", "3" to "3"),
                    listOf("1" to "1", "1.5" to "1.5", "2" to "2.5", "3" to "3")
                ),
                //modify+add after
                Arguments.of(
                    listOf("1" to "1", "2" to "2", "3" to "3"),
                    listOf("1" to "1", "2" to "2.5", "2.7" to "2.7", "3" to "3")
                ),
                //modify+remove before
                Arguments.of(
                    listOf("1" to "1", "2" to "2", "3" to "3"),
                    listOf("2" to "2.5", "3" to "3")
                ),
                //modify+remove after
                Arguments.of(
                    listOf("1" to "1", "2" to "2", "3" to "3"),
                    listOf("1" to "1", "2" to "2.5")
                ),
                //modify+move
                Arguments.of(
                    listOf("1" to "1", "2" to "2", "3" to "3"),
                    listOf("3" to "3", "2" to "2.5", "1" to "1")
                ),
                //modify+move+add after
                Arguments.of(
                    listOf("1" to "1", "2" to "2", "3" to "3"),
                    listOf("3" to "3", "2" to "2.5", "2.7" to "2.7", "1" to "1")
                ),
                //modify+move+add before
                //modify+move+remove after
                //modify+move+remove before
            )
        }
    }

    @ParameterizedTest
    @MethodSource("generateTestData")
    fun `list update test`(oldList: List<String>, newList: List<String>) {
        val listToApply = ArrayList(oldList)
        val updateMessage = ListUpdateUtil.calculateDiff(
            oldList,
            newList,
            { oldItem, newItem -> oldItem == newItem }
        )
        ListUpdateUtil.applyDiff(listToApply, updateMessage)
        assert(listToApply == newList) {
            println("lists are different, actual list: ")
            println(listToApply)
            println("expected list: ")
            println(newList)
            println("old list: ")
            println(oldList)
            println("update message: ${updateMessage.toString { it }}")
        }
    }

    @ParameterizedTest
    @MethodSource("generateModifyItemTestData")
    fun `list modify item update test`(
        oldList: List<TestPair<String>>,
        newList: List<TestPair<String>>,
    ) {
        val listToApply = ArrayList(oldList)
        val updateMessage = ListUpdateUtil.calculateDiff(
            oldList,
            newList,
            { oldItem, newItem -> oldItem.second == newItem.second }
        )
        ListUpdateUtil.applyDiff(listToApply, updateMessage)
        assert(listToApply == newList &&
            listToApply.filterIndexed { index, item -> newList[index].second != item.second }
                .isEmpty()
        ) {
            println("lists are different, actual list: ")
            println(listToApply)
            println("expected list: ")
            println(newList)
            println("old list: ")
            println(oldList)
            println("update message: ${updateMessage.toString { "[${it.first}] = ${it.second}" }}")
        }
    }

    class TestPair<T>(
        val first: T,
        val second: T
    ) {
        override fun toString(): String = "[$first] = $second"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestPair<*>

            return first == other.first
        }

        override fun hashCode(): Int {
            return first?.hashCode() ?: 0
        }

    }


}

private infix fun <T> T.to(that: T): TestPair<T> = TestPair(this, that)
