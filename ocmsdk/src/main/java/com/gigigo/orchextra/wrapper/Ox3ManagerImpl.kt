package com.gigigo.orchextra.wrapper

import android.app.Application
import com.gigigo.orchextra.core.Orchextra
import com.gigigo.orchextra.core.OrchextraErrorListener
import com.gigigo.orchextra.core.OrchextraOptions
import com.gigigo.orchextra.core.OrchextraStatusListener
import com.gigigo.orchextra.core.domain.actions.actionexecutors.customaction.OrchextraCustomActionListener
import com.gigigo.orchextra.core.domain.entities.Error
import com.gigigo.orchextra.geofence.OxGeofenceImp
import com.gigigo.orchextra.indoorpositioning.OxIndoorPositioningImp
import com.gigigo.orchextra.wrapper.OxManager.CustomActionListener
import com.gigigo.orchextra.wrapper.OxManager.ErrorListener
import com.gigigo.orchextra.wrapper.OxManager.StatusListener
import com.gigigo.orchextra.wrapper.OxManager.TokenReceiver

class Ox3ManagerImpl : OxManager {

  private val orchextra = Orchextra
  private val genders: HashMap<CrmUser.Gender, String> = HashMap()

  init {
    genders[CrmUser.Gender.GenderFemale] = "female"
    genders[CrmUser.Gender.GenderMale] = "male"
    genders[CrmUser.Gender.GenderND] = "nd"
  }

  override fun startImageRecognition() = orchextra.openImageRecognition()

  override fun startScanner() = orchextra.openScanner()

  override fun init(application: Application, config: OxConfig, statusListener: StatusListener) {

    orchextra.setStatusListener(object : OrchextraStatusListener {
      override fun onStatusChange(isReady: Boolean) {
        if (isReady) {
          orchextra.getTriggerManager().geofence = OxGeofenceImp.create(application)
          orchextra.getTriggerManager()
              .indoorPositioning = OxIndoorPositioningImp.create(application)

          config.notificationActivityClass?.let {
            orchextra.setNotificationActivityClass(it)
          }

          statusListener.isReady()
        } else {
          statusListener.onError("SDK isn't ready")
        }
      }
    })

    orchextra.setErrorListener(object : OrchextraErrorListener {
      override fun onError(error: Error) {
        statusListener.onError(error.message)
      }
    })

    val options = OrchextraOptions.Builder().firebaseApiKey(config.firebaseApiKey)
        .firebaseApplicationId(config.firebaseApplicationId)
        .debuggable(true)
        .build()

    orchextra.init(application, config.apiKey, config.apiSecret, options)
    orchextra.setScanTime(30)
  }

  override fun finish() = orchextra.finish()

  override fun removeListeners() = with(orchextra) {
    removeStatusListener()
    removeErrorListener()
  }

  override fun isReady(): Boolean = orchextra.isReady()

  override fun getToken(tokenReceiver: TokenReceiver) =
      orchextra.getToken { token -> tokenReceiver.onGetToken(token) }

  override fun setErrorListener(errorListener: ErrorListener) {
    orchextra.setErrorListener(object : OrchextraErrorListener {
      override fun onError(error: Error) {
        errorListener.onError(error.message)
      }
    })
  }

  override fun setBusinessUnits(businessUnits: List<String>) {
    orchextra.getCrmManager().setDeviceData(null, businessUnits)
  }

  override fun setCustomSchemeReceiver(customSchemeReceiver: CustomActionListener) {
    orchextra.setCustomActionListener(object : OrchextraCustomActionListener {
      override fun onCustomSchema(customSchema: String) {
        customSchemeReceiver.onCustomSchema(customSchema)
      }
    })
  }

  companion object {
    private val TAG = "Ox3ManagerImp"
  }
}