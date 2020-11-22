package com.example.yeebum.screens.bulb_control

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.yeebum.R
import com.example.yeebum.control_bulb.ChooseValue
import com.example.yeebum.control_bulb.Constants.CMD_BRIGHTNESS
import com.example.yeebum.control_bulb.Constants.CMD_CRON_ADD
import com.example.yeebum.control_bulb.Constants.CMD_CT
import com.example.yeebum.control_bulb.Constants.CMD_HSV
import com.example.yeebum.control_bulb.Constants.CMD_OFF
import com.example.yeebum.control_bulb.Constants.CMD_ON
import com.example.yeebum.control_bulb.Constants.ID
import com.example.yeebum.screens.components.Helpers
import com.example.yeebum.screens.components.LoadingDialog
import kotlinx.android.synthetic.main.fragment_color_control.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.net.ConnectException
import java.net.Socket


class ColorControlFragment : Fragment() , ChooseValue {

    private lateinit var socket: Socket
    private lateinit var mBos: BufferedOutputStream
    private val helpers = Helpers()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_color_control, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupColorPicker()
        connectToBulb()


        //-----------------| Turn On\Off Button |--------------------
        var isOn = false
        onOffButton.setOnClickListener {
            isOn = !isOn
            toggleSwitch(isOn)
        }
        //===========================================================

        setupBrightness()
        setupColorTemp()
        setupHueColorPickerDialog()
        setupDurationPickerDialog()
    }

    //-------------------------------| Connect to the bulb |----------------------------------
    @Suppress("BlockingMethodInNonBlockingContext")
    private fun connectToBulb() {

        val dialog = LoadingDialog.getDialog(requireContext(), "Connecting...")
        dialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = Socket("192.168.0.108", 55443)
                socket.keepAlive = true
                mBos = BufferedOutputStream(socket.getOutputStream())
            } catch (connectEx: ConnectException) {
                helpers.showSnackBar(
                    requireView(),
                    "Error! Check your internet connection.",
                    null,
                    null
                )
            } catch (ex: Exception) {
                helpers.showSnackBar(requireView(), ex.message!!, null, null)
            }
            dialog.dismiss()
        }
    }
    //========================================================================================


    //----------------------| Write Command |--------------------------
    private fun write(cmd: String) {
        if (socket.isConnected)
            executeAction(cmd)
        else
            helpers.showSnackBar(requireView(), "Check your bulb ip and port", null, null)
        //Log.d("TAG",cmd)
    }
    //=================================================================



    //---------------------------| Execute write command action |---------------------------
    @Suppress("BlockingMethodInNonBlockingContext")
    private fun executeAction(cmd: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mBos.write(cmd.toByteArray())
            mBos.flush()
        }
    }
    //======================================================================================


    //-------------------------| Change bulb to on or Off state |-------------------------------------
    private fun toggleSwitch(isOn: Boolean) {
        onOffButton.setColorFilter(if (isOn) Color.argb(255, 30, 255, 30) else Color.argb(255, 255, 30, 30))
        write(if (isOn) CMD_ON.replace("%id", ID) else CMD_OFF.replace("%id", ID))
    }
    //================================================================================================




    //--------------| Setup Wheel Color Picker Command |----------------
    private fun setupColorPicker() {
        colorPicker.showOldCenterColor = false
        colorPicker.setOnColorChangedListener {
            ViewCompat.setBackgroundTintList(
                currentHueColor,
                ColorStateList.valueOf(it))
        }

        colorPicker.setOnColorSelectedListener {
            val hsv = FloatArray(3)
            Color.colorToHSV(it, hsv)
            write(CMD_HSV.replace("%id", ID)
            .replace("%value", hsv[0].toString()))

        }
    }
    //===========================================================



    //------------------------------| Set brightness on seekBar stop |----------------------------------
    private fun setupBrightness(){
        brightnessSeekBar.apply {
            max = 99
            setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    brightnessPercentNumber.text = "${p1+1}%"
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    write(
                        CMD_BRIGHTNESS.replace("%id", ID)
                        .replace("%value", (p0!!.progress+1).toString()))
                }

            })
        }
    }
    //==================================================================================================



    //-----------------------------| Change Color Temperature |--------------------------------

    private fun setupColorTemp(){
        arrayListOf<View>(colorTempImage,colorTempText,colorTempValueText)
            .forEach {
                it.setOnClickListener {
                    val dialog = helpers.getSeekBarColorTempDialog(
                        requireActivity(),requireContext(),"Choose Temp","Choose Temp Of your yeelight",this
                    )
                    dialog.show()
                }
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onSetColorTemp(temp: Int) {
        colorTempValueText.text = "$temp k"
        write(
            CMD_CT.replace("%id", ID).replace("%value", temp.toString()))
    }

    //==========================================================================================



    //----------------------------------| Show color picker dialog |--------------------------------------------

    private fun setupHueColorPickerDialog(){
        arrayListOf<View>(hueImage,hueText,currentHueColor)
            .forEach {
                it.setOnClickListener {
                    val dialog = helpers.getChooseColorDialog(requireActivity(),requireContext(),"Choose Color",this)
                    dialog.show()
                }
            }
    }

    override fun onSetColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        write(CMD_HSV.replace("%id", ID)
            .replace("%value", hsv[0].toString()))
    }


    //============================================================================================================


    //-------------------------------| Setup time picker |-----------------------------
    private fun setupDurationPickerDialog(){
        arrayListOf<View>(durationImage,durationText,durationValueText, durationImage2)
            .forEach {
                it.setOnClickListener {
                    val dialog = helpers.getDurationPickerDialog(requireActivity(),requireContext(),"Choose Duration", this)
                    dialog.show()
                }
            }
    }

    override fun onSetDuration(time: Int) {
        write(
            CMD_CRON_ADD.replace("%id", ID)
            .replace("%value",time.toString()))
    }
    //==================================================================================

}
