package com.github.anrimian.musicplayer.data.repositories.library.edit

import org.junit.jupiter.api.Test

class EditorRepositoryImplTest {

    @Test
    fun `change composition filename test checklist`() {
        /*
        Checklist:
        after api 30
        after api 30 - with duplicate file
        after api 30 - check playlist path
        ---------
        before api 30
        before api 30 - with duplicate file
        before api 30 - check playlist path
        --------- SPECIAL CASE
        with duplicate hidden file - how to create case?
        */
    }

    @Test
    fun `change folder name test checklist`() {
        /*
        Checklist:
        after api 30
        after api 30 - with duplicate folder
        after api 30 - with duplicate hidden folder
        after api 30 - with duplicate file in duplicate hidden folder
        after api 30 - with duplicate folder name in path, like A/B/A
        after api 30 - check playlist path
        ---------
        before api 30
        before api 30 - with duplicate folder
        before api 30 - with duplicate hidden folder
        before api 30 - with duplicate file in duplicate hidden folder
        before api 30 - with duplicate folder name in path, like A/B/A
        before api 30 - check playlist path
        ---------
        NOTE duplicate folder name checks:
        base: A/B/A/B/C.xz
        1) -> A/B/A2/B/c.xz
        1) -> A2/B/A/B/c.xz
        ---------
        NOTE duplicate files check
        base: 2 files: A_1/file.xz, A_2/file.xz(hidden)
        1) -> rename A_1/file.xz to A_2/file.xz
        */
    }

    @Test
    fun `move files test checklist`() {
        /*
        Checklist:
        after api 30
        after api 30 - move in the source folder(no move case)
        after api 30 - move inside source folder
        after api 30 - duplicate file in destination
        after api 30 - move from root folder
        after api 30 - move to root folder(not allowed)
        ---------
        before api 30
        before api 30 - move in the source folder(no move case)
        before api 30 - move inside source folder
        before api 30 - duplicate file in destination
        before api 30 - move from root folder
        before api 30 - move to root folder
        ---------
        duplicate hidden file in destination - how to create this case?
        move inside source folder case:
        A/(B) -> A/B/(B) = expect error
        A/(B) -> A/B2/(B) = expect complete
        A/(B) -> A/B/A/(B) = expect error
        */
    }


    @Test
    fun `move files to new directory test checklist`() {
        /*
        Checklist:
        after api 30
        after api 30 - move in already exists folder
        after api 30 - move in already exists hidden folder
        after api 30 - duplicate file in destination folder
        after api 30 - duplicate file in hidden destination folder
        after api 30 - move inside source folder
        after api 30 - move from root folder
        after api 30 - move in new root folder(not allowed)
        ---------
        before api 30
        before api 30 - move in already exists folder
        before api 30 - move in already exists hidden folder
        before api 30 - duplicate file in destination folder
        before api 30 - duplicate file in hidden destination folder
        before api 30 - move inside source folder
        before api 30 - move from root folder
        before api 30 - move in new root folder
        ---------
        move inside source folder case:
        A/(B) -> A/B/C/(B) = expect error
        A/(B) -> A/B2/C/(B) = expect complete
        */
    }

}