package mobi.audax.tupi.passageiro.activities.intro

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPagerBuilder
import mobi.audax.tupi.passageiro.R
import mobi.audax.tupi.passageiro.activities.bem_vindo.BemVindoActivity
import mobi.audax.tupi.passageiro.activities.home.HomeActivity
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo
import mobi.audax.tupi.passageiro.bin.util.Permission
import mobi.audax.tupi.passageiro.bin.util.Prefs
import mobi.audax.tupi.passageiro.bin.util.Util

class PermissionActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isVibrate = true
        vibrateDuration = 50L
        isWizardMode = true
        isColorTransitionsEnabled = true
        setImmersiveMode()
        setTransformer(AppIntroPageTransformerType.Fade)

        addSlide(
                AppIntroFragment.createInstance(
                        title = getString(R.string.app_name),
                        description = getString(R.string.app_description),
                        imageDrawable = R.mipmap.ic_launcher_round,
                        backgroundColorRes = R.color.slide_color_0,
                        descriptionTypefaceFontRes = R.font.ubuntu
                )
        )

        val permission = Permission(this)
        var SLIDE_NUMBER = 1
        // region camera
        if (!permission.hasCameraPermission()) {
            addSlide(
                    AppIntroFragment.createInstance(
                            SliderPagerBuilder()
                                    .title(getText(R.string.camera))
                                    .description(getString(R.string.uso_permissao_camera))
                                    .imageDrawable(R.drawable.ic_intro_camera)
                                    .backgroundColorRes(R.color.slide_color_1)
                                    .descriptionTypefaceFontRes(R.font.ubuntu)
                                    .build()
                    )
            )
            askForPermissions(
                    permissions = arrayOf(Manifest.permission.CAMERA),
                    slideNumber = ++SLIDE_NUMBER,
                    required = true
            )
        }
        // endregion
        // region write external storage
        if (!permission.hasWriteExternalStoragePermission()) {
            addSlide(
                    AppIntroFragment.createInstance(
                            SliderPagerBuilder()
                                    .title(getText(R.string.write_external_storage))
                                    .description(getString(R.string.uso_permissao_write_external_storage))
                                    .imageDrawable(R.drawable.ic_intro_write_external_storage)
                                    .backgroundColorRes(R.color.slide_color_2)
                                    .descriptionTypefaceFontRes(R.font.ubuntu)
                                    .build()
                    )
            )
            askForPermissions(
                    permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    slideNumber = ++SLIDE_NUMBER,
                    required = true
            )
        }
        // endregion
        // region location
        if (!permission.hasLocationPermission()) {
            addSlide(
                    AppIntroFragment.createInstance(
                            SliderPagerBuilder()
                                    .title(getText(R.string.access_fine_location))
                                    .description(getString(R.string.uso_permissao_access_fine_location))
                                    .imageDrawable(R.drawable.ic_intro_access_fine_location)
                                    .backgroundColorRes(R.color.slide_color_3)
                                    .descriptionTypefaceFontRes(R.font.ubuntu)
                                    .build()
                    )
            )
            val perms: MutableList<String> = ArrayList()
            if (!permission.hasLocationPermission()) {
                perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            askForPermissions(perms.toTypedArray(), ++SLIDE_NUMBER, true)
        }
        // endregion

        addSlide(
                AppIntroFragment.createInstance(
                        title = getString(R.string.pronto),
                        description = getString(R.string.permission_activity_sucesso),
                        imageDrawable = R.mipmap.ic_launcher_round,
                        backgroundColorRes = R.color.slide_color_0,
                        descriptionTypefaceFontRes = R.font.ubuntu
                )
        )

    }

    override fun onResume() {
        super.onResume()
        setImmersiveMode()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        startActivity(Intent(this, BemVindoActivity::class.java))
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val prefs = Prefs(this)
        val isPrimeiroAcesso = prefs.isPrimeiroAcesso
        if (isPrimeiroAcesso) {
            prefs.isPrimeiroAcesso = false
            startActivity(Intent(this, BemVindoActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onUserDeniedPermission(permissionName: String) {
        super.onUserDeniedPermission(permissionName)
        setImmersiveMode()
        this.msgPermissaoNegada()
    }

    override fun onUserDisabledPermission(permissionName: String) {
        super.onUserDisabledPermission(permissionName)
        setImmersiveMode()
        var p = ""
        when (permissionName) {
            Manifest.permission.CAMERA -> p = getString(R.string.camera)
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> p = getString(R.string.write_external_storage)
            Manifest.permission.ACCESS_FINE_LOCATION -> p = getString(R.string.access_fine_location)
        }
        val body: String = getString(R.string.msg_permissao_negada_desabilitada, p)
        Util.showAlertDialog(body, this) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${packageName}")))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setImmersiveMode()
    }

    private fun msgPermissaoNegada() {
        Toast.makeText(this, R.string.msg_permissao_negada, Toast.LENGTH_LONG).show()
    }

}