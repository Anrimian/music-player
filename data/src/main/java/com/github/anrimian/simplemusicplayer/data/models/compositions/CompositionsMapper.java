package com.github.anrimian.simplemusicplayer.data.models.compositions;

import com.github.anrimian.simplemusicplayer.data.database.models.CompositionEntity;
import com.github.anrimian.simplemusicplayer.data.utils.mappers.DateMapper;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Created on 19.11.2017.
 */

@Mapper(uses = DateMapper.class)
public interface CompositionsMapper {

    Composition toComposition(CompositionEntity compositionEntity);

    CompositionEntity toCompositionEntity(Composition composition);

    @IterableMapping(elementTargetType = Composition.class)
    List<Composition> toCompositions(List<CompositionEntity> compositionEntities);

    @IterableMapping(elementTargetType = CompositionEntity.class)
    List<CompositionEntity> toCompositionEntityList(List<Composition> compositions);
}
