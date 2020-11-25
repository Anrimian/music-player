package com.github.anrimian.musicplayer.data.utils.folders;

import org.junit.Test;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

import static org.junit.Assert.assertEquals;

public class RxNodeTest {

    private RxNode<Integer> root = new RxNode<>(0, new StringNode("root"));

    @Test
    public void addChildTest() {
        TestObserver<List<RxNode<Integer>>> childObserver = root
                .getChildObservable()
                .test();

        RxNode<Integer> rootChild1 = new RxNode<>(1, new StringNode("child 1"));
        root.addNode(rootChild1);

        childObserver.assertValueAt(1, list -> {
            assertEquals(1, list.size());
            assertEquals(rootChild1, list.get(0));

            return true;
        });

        assert root.getNodes().contains(rootChild1);

    }
}