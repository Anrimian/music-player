package com.github.anrimian.musicplayer.data.utils.folders;

public class RxNodeTest {
/*    private FolderTree folderTree;

    private FolderDataSource folderDataSource = mock(FolderDataSource.class);

    private PublishSubject<Change<Folder>> changeFolderSubject = PublishSubject.create();

    private Folder folder1 = new Folder();
    private Folder folder11 = new Folder();
    private Folder folder111 = new Folder();

    @Before
    public void setUp() throws Exception {
        folder1.setLocalId(1);

        folder11.setLocalId(2);
        folder11.setParentLocalId(1L);

        folder111.setLocalId(3);
        folder111.setParentLocalId(2L);

        when(folderDataSource.getAllFolders()).thenReturn(asList(folder1, folder11, folder111));
        when(folderDataSource.getFolderChangeObservable()).thenReturn(changeFolderSubject);

        folderTree = new FolderTree(folderDataSource);
    }

    @Test
    public void getFoldersTest() throws Exception {
        folderTree.getFolders(null)
                .test()
                .assertValue(treeInfo -> treeInfo.getNodes().size() == 1);

        folderTree.getFolders(folder1)
                .test()
                .assertValue(folders -> folders.getNodes().size() == 1);
    }

    @Test
    public void addFolderTest() throws Exception {
        TestObserver<Change<FolderNode>> testObserver = folderTree.getFolders(folder1)
                .blockingGet()
                .getFilesObservable()
                .test();

        Folder folder12 = new Folder();
        folder12.setLocalId(22);
        folder12.setParentLocalId(1L);
        changeFolderSubject.onNext(new Change<>(ChangeType.ADDED, folder12));

        testObserver.assertValueAt(0, change -> change.getData()
                .getFolder()
                .getLocalId() == 22 && change.getChangeType() == ChangeType.ADDED);

        testObserver.assertValueAt(1, change -> change.getData()
                .getFolder()
                .getLocalId() == 1 && change.getChangeType() == ChangeType.MODIFY);

    }

    @Test
    public void modifyFolderTest() throws Exception {
        TestObserver<Change<FolderNode>> testObserver = folderTree.getFolders(folder1)
                .blockingGet()
                .getFilesObservable()
                .test();
        TestObserver<Change<FolderNode>> testRootObserver = folderTree.getFolders(null)
                .blockingGet()
                .getFilesObservable()
                .test();

        folder11.setName("test");

        changeFolderSubject.onNext(new Change<>(ChangeType.MODIFY, folder11));

        testObserver.assertValue(change -> change.getData()
                .getFolder()
                .getLocalId() == 2
                && change.getChangeType() == ChangeType.MODIFY
                && Objects.equals(change.getData()
                .getFolder()
                .getName(), "test"));

        testRootObserver.assertValue(change -> change.getData()
                .getFolder()
                .getLocalId() == 1
                && change.getChangeType() == ChangeType.MODIFY);
    }

    @Test
    public void removeFolderTest() throws Exception {
        TestObserver<Change<FolderNode>> testObserver = folderTree.getFolders(folder1)
                .blockingGet()
                .getFilesObservable()
                .test();

        changeFolderSubject.onNext(new Change<>(ChangeType.DELETED, folder11));

        testObserver.assertValueAt(0, change -> change.getData()
                .getFolder()
                .getLocalId() == 2 && change.getChangeType() == ChangeType.DELETED);

        testObserver.assertValueAt(1, change -> change.getData()
                .getFolder()
                .getLocalId() == 1 && change.getChangeType() == ChangeType.MODIFY && !change.getData().isHasChildren());
    }

    @Test
    public void removeChildFolderTest() throws Exception {
        TestObserver<Change<FolderNode>> testObserver = folderTree.getFolders(folder1)
                .blockingGet()
                .getFilesObservable()
                .test();

        changeFolderSubject.onNext(new Change<>(ChangeType.DELETED, folder111));

        testObserver.assertValue(change -> {
            assertEquals(2, change.getData().getFolder().getLocalId());
            assertEquals(ChangeType.MODIFY, change.getChangeType());
            assertEquals(false, change.getData().isHasChildren());
            return true;
        });
    }*/
}