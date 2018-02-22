package com.gigigo.orchextra.core.data.api.mappers.elementcache

import android.util.Log
import com.gigigo.ggglib.mappers.ExternalClassToModelMapper
import com.gigigo.orchextra.core.data.api.dto.elementcache.ApiElementCache
import com.gigigo.orchextra.core.data.api.mappers.elements.ApiElementSegmentationMapper
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCacheType
import java.util.ArrayList

class ApiElementCacheMapper(
    private val apiElementCacheItemRenderMapper: ApiElementCacheRenderMapper,
    private val apiElementCachePreviewMapper: ApiElementCachePreviewMapper,
    private val apiElementCacheShareMapper: ApiElementCacheShareMapper,
    private val apiElementSegmentationMapper: ApiElementSegmentationMapper) : ExternalClassToModelMapper<ApiElementCache, ElementCache> {

  override fun externalClassToModel(data: ApiElementCache): ElementCache {
    val time = System.currentTimeMillis()

    val model = ElementCache()

    with(model) {
      slug = data.slug
      type = ElementCacheType.convertStringToEnum(data.type)
      name = data.name
      updateAt = data.updatedAt
    }

    val tagList = ArrayList<String>()

    with(data) {
      tags?.let {
        tagList += tags
        model.tags = tagList
      }

      render?.let {
        model.render = apiElementCacheItemRenderMapper.externalClassToModel(render)
      }

      preview?.let {
        model.preview = apiElementCachePreviewMapper.externalClassToModel(preview)
      }

      share?.let {
        model.share = apiElementCacheShareMapper.externalClassToModel(share)
      }

      segmentation?.let {
        model.segmentation = apiElementSegmentationMapper.externalClassToModel(segmentation)
      }
    }

    val currentTime = System.currentTimeMillis() - time
    Log.v("TT - ApiElementCache", ("" + currentTime/1000))

    return model
  }
}
