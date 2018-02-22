package com.gigigo.orchextra.core.data.api.mappers.elements

import android.util.Log
import com.gigigo.ggglib.mappers.ExternalClassToModelMapper
import com.gigigo.orchextra.core.data.api.dto.elements.ApiElement
import com.gigigo.orchextra.core.domain.entities.elements.Element
import java.util.ArrayList

class ApiElementMapper(private val apiMenuItemViewMapper: ApiElementSectionViewMapper) : ExternalClassToModelMapper<ApiElement, Element> {

  override fun externalClassToModel(data: ApiElement): Element? {
    val time = System.currentTimeMillis()

    val model = Element()

    if (data.sectionView == null || data.elementUrl == null) {
      return null
    }

    with(model) {
      slug = data.slug
      elementUrl = data.elementUrl
      name = data.name
      dates = data.dates
    }

    with(data) {
      customProperties?.let {
        model.customProperties = customProperties
      }

      sectionView?.let {
        model.sectionView = apiMenuItemViewMapper.externalClassToModel(sectionView)
      }

      val tagList = ArrayList<String>()
      tags?.let {
        for (tag in tags) {
          tagList.add(tag)
        }
      }
      model.tags = tagList
    }

    val currentTime = System.currentTimeMillis() - time
    Log.v("TT - ApiElement", ("" + currentTime/1000))


    return model
  }
}
