package com.github.anrimian.simplemusicplayer.data.repositories.music;


import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.schedulers.Schedulers;

/**
 * Created on 28.10.2017.
 */
@RunWith(AndroidJUnit4.class)
public class MusicProviderRepositoryImplTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private MusicProviderRepository musicProviderRepository;

    @Before
    public void before() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        musicProviderRepository = new MusicProviderRepositoryImpl(appContext, Schedulers.io());
    }

    @Test
    public void testRepositoryReturnValues() {
        List<Composition> compositions = musicProviderRepository.getAllCompositions().blockingGet();
        for (Composition composition: compositions) {
            Assert.assertNotNull(composition.getFilePath());
        }
    }

}