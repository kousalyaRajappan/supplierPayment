package za.co.topitup.suppliers.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.zj.usbsdk.UsbController
import wangpos.sdk4.libbasebinder.Printer
import za.co.topitup.suppliers.CashManActivity
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.models.MyItem
import za.co.topitup.suppliers.models.Payment
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.sdk.Command
import kotlin.concurrent.thread


private const val TAG = "Printer"

class Print(appContext: Context){
    private lateinit var usbCtrl: UsbController
    private   var dev: UsbDevice? = null


    private val ACTION_USB_PERMISSION = "za.co.topitup.suppliers.USB_PERMISSION"

    private val center = Printer.Align.CENTER
    //    private val right = Printer.Align.RIGHT
    private val left = Printer.Align.LEFT
    private val defaultFont = Printer.Font.DEFAULT
    private val boldFont = Printer.Font.DEFAULT_BOLD

    private var printer: Printer? = null
    private var context: Context = appContext

    private var loop = false
    private var threadRunning = false
    private var printerExists = true

    init {
        thread(isDaemon = true) {
            printer = Printer(context)
            logger(TAG, "Printer Init: $printer")
            try {
                printer?.printInit()
                printer?.clearPrintDataCache()

            }  catch (e: RemoteException) {
                e.printStackTrace()
                printerExists = false
            }
        }
    }

    private fun printDefaultReceipt(payment: Payment, isReprint: Boolean) {
        val defaultFontSize = context.resources.getInteger(R.integer.receipt_default_font_size)

        val companyName = context.getString(R.string.company_name)
        val phoneNumber = context.getString(R.string.company_phone)
        val reprint = context.getString(R.string.reprintLabel)
        val website = context.getString(R.string.company_website)
        val receiptNumber =
            context.getString(R.string.receipt_number_label).plus(" ").plus(payment.id)


        val supplierName = payment.supplierName
        //TODO Split Date into date and time instead.
        val date = payment.date
        val time = payment.time
        val cashier = payment.cashier
        val accountNumber = payment.account
        val customerNumber = payment.customerNumber
        val reference = payment.paymentReference
        //TODO extension to proper format
        val amount = toCurrency(payment.amount)
        logger(TAG, "Printing printDefaultReceipt")

        try {
            if(Retailer.connected.equals("usb")){
                Command.ESC_Align[2] = 0x01.toByte()
                usbCtrl.sendByte(Command.ESC_Align, dev)
                usbCtrl.sendMsg(supplierName, "GBK", dev)

// Reprint flag
                if (isReprint) {
                    usbCtrl.sendMsg("\n\n\n\n", "GBK", dev)

                    usbCtrl.sendMsg(reprint, "GBK", dev)
                }

                usbCtrl.sendMsg("\n\n\n\n", "GBK", dev)

// Receipt Number - center aligned and bold
                Command.ESC_Align[2] = 0x01.toByte()
                usbCtrl.sendByte(Command.ESC_Align, dev)
                usbCtrl.sendMsg(receiptNumber, "GBK", dev)

                usbCtrl.sendMsg("\n\n\n\n", "GBK", dev)

// Left align for details
                Command.ESC_Align[2] = 0x00.toByte()
                usbCtrl.sendByte(Command.ESC_Align, dev)
                usbCtrl.sendMsg("Date" + getWhiteSpace(32 - 4 - 4) + "Time", "GBK", dev)
                usbCtrl.sendMsg(date + getWhiteSpace(32 - date!!.length - time!!.length) + time, "GBK", dev)
                usbCtrl.sendMsg("Cashier" + getWhiteSpace(32 - 7 - cashier!!.length) + cashier, "GBK", dev)

                usbCtrl.sendMsg("\n\n\n\n", "GBK", dev)

// Account, Customer, Reference, Amount
                usbCtrl.sendMsg("Account #:" + getWhiteSpace(32 - 10 - accountNumber!!.length) + accountNumber, "GBK", dev)
                usbCtrl.sendMsg("Customer #:" + getWhiteSpace(32 - 11 - customerNumber!!.length) + customerNumber, "GBK", dev)
                usbCtrl.sendMsg("Reference:" + getWhiteSpace(32 - 10 - reference!!.length) + reference, "GBK", dev)
                usbCtrl.sendMsg("Amount:" + getWhiteSpace(32 - 7 - amount.length) + amount, "GBK", dev)

                usbCtrl.sendMsg("\n\n\n\n", "GBK", dev)

// Footer - center align
                Command.ESC_Align[2] = 0x01.toByte()
                usbCtrl.sendByte(Command.ESC_Align, dev)
                usbCtrl.sendMsg(companyName, "GBK", dev)
                usbCtrl.sendMsg(phoneNumber, "GBK", dev)
            }else if(Retailer.connected.equals("bluetooth")){
                val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
                val alignLeft = byteArrayOf(0x1B, 0x61, 0x00)

                SendBytes(alignCenter)
                SendText("$supplierName\n")
                if (isReprint) {
                    SendText("$reprint\n\n")
                }

                SendBytes(alignLeft)
                SendText("Date${getWhiteSpace(32 - 8)}Time\n")
                SendText("$date${getWhiteSpace(32 - date!!.length - time!!.length)}$time\n")

                SendText("Cashier" + getWhiteSpace(32 - 7 - cashier!!.length) + cashier + "\n")


                SendText("\n\n\n\n")

// Account, Customer, Reference, Amount
                SendText("Account #:" + getWhiteSpace(32 - 10 - accountNumber!!.length) + accountNumber + "\n")
                SendText("Customer #:" + getWhiteSpace(32 - 11 - customerNumber!!.length) + customerNumber + "\n")
                SendText("Reference:" + getWhiteSpace(32 - 10 - reference!!.length) + reference + "\n")
                SendText("Amount:" + getWhiteSpace(32 - 7 - amount.length) + amount + "\n")
                SendBytes(alignCenter)
                SendText("$companyName\n")
                SendText("$phoneNumber\n")
                SendText("$website\n")
                SendText("\n\n\n\n\n\n") // Simulate `printPaper(80)`

                SendBytes(alignLeft)
            }else{
                printer?.printInit()
                printer?.let { thisPrinter ->
                    thisPrinter.clearPrintDataCache()
                    thisPrinter.printString(supplierName, 30, center, true, false)
                    if (isReprint) {
                        thisPrinter.printPaper(5)
                        thisPrinter.printString(reprint, 28, center, false, true)
                    }
                    thisPrinter.printPaper(10)
                    thisPrinter.printString(
                        receiptNumber, boldFont, 28, center,
                        false, false, false
                    )
                    thisPrinter.printPaper(15)
                    thisPrinter.print2StringInLine(
                        "Date", "Time", 1.0f, defaultFont, defaultFontSize,
                        left, false, true, false
                    )
                    thisPrinter.print2StringInLine(
                        date, time, 1.0f, boldFont, defaultFontSize,
                        left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Cashier", cashier, 1.0f, defaultFont,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.printPaper(10)

                    thisPrinter.print2StringInLine(
                        "Account #:", accountNumber, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Customer #:", customerNumber, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Reference:", reference, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Amount:", amount, 1.0f, boldFont,
                        defaultFontSize, left, true, false, true
                    )

                    //Footer
                    thisPrinter.printPaper(20)
                    thisPrinter.printString(companyName, 25, center, false, false)
                    thisPrinter.printString(phoneNumber, 28, center, false, false)
                    thisPrinter.printString(website, 25, center, false, false)

                    //TODO Update SDK to use this function
                    //printer.printMultiseriateString()

                    thisPrinter.printPaper(80)
                }
            }


        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun printSupplierDefaultReceipt(payment: MyItem, isReprint: Boolean) {
        val defaultFontSize = context.resources.getInteger(R.integer.receipt_default_font_size)

        val companyName = context.getString(R.string.company_name)
        val phoneNumber = context.getString(R.string.company_phone)
        val reprint = context.getString(R.string.reprintLabel)
        val website = context.getString(R.string.company_website)
        /* val receiptNumber =
             context.getString(R.string.receipt_number_label).plus(" ").plus(payment.id)*/
        val mString = payment.btn_date!!.split(",").toTypedArray()


        val supplierName = payment.supSupplierName
        //TODO Split Date into date and time instead.
        val date = mString[0]
        val time = mString[1]
//        val cashier = payment.cashier
//        val accountNumber = payment.account
        val customerNumber = payment.supCustomer
        val shipment = payment.supShipment
        val driver = payment.sup_driver
        val drivercell = payment.sup_driver_cell

        val reference = payment.supplierRef
        //TODO extension to proper format
        val amount = payment.supAmount

        try {
            logger(TAG, "Printing printDefaultReceipt")

            if(Retailer.deviceTypeTo.equals("Mobile")){

                if(Retailer.connected.equals("usb")){
                    Log.e("supplier name.......","supplier..........."+supplierName)

                    Command.ESC_Align[2] = 0x01.toByte()
                    usbCtrl.sendByte( Command.ESC_Align,dev)
                    usbCtrl.sendMsg(supplierName, "GBK", dev)
                    usbCtrl.sendMsg(reprint+"\n", "GBK", dev)
                    Command.ESC_Align[2] = 0x00.toByte()

                    usbCtrl.sendByte( Command.ESC_Align,dev)

                    usbCtrl.sendMsg("Date"+""+getWhiteSpace(32 -8)+"Time", "GBK", dev)
                    usbCtrl.sendMsg(date+""+getWhiteSpace(32-date.length-time.length)+""+time, "GBK", dev)
                    usbCtrl.sendMsg("Reference:"+""+getWhiteSpace(32-10-reference.length)+""+reference, "GBK", dev)
                    usbCtrl.sendMsg("Customer #:"+""+getWhiteSpace(32-11-customerNumber.length)+""+customerNumber, "GBK", dev)
                    usbCtrl.sendMsg("Shipment #:"+""+getWhiteSpace(32-11-shipment.length)+""+shipment, "GBK", dev)
                    usbCtrl.sendMsg("Driver #:"+""+getWhiteSpace(32-9-driver.length)+""+driver, "GBK", dev)
                    usbCtrl.sendMsg("Driver Cell #:"+""+getWhiteSpace(32-14-drivercell.length)+""+drivercell, "GBK", dev)
                    usbCtrl.sendMsg("Amount #:"+""+getWhiteSpace(32-9-amount.length)+""+amount, "GBK", dev)


                    Command.ESC_Align[2] = 0x01.toByte()
                    usbCtrl.sendByte( Command.ESC_Align,dev)
                    usbCtrl.sendMsg(companyName, "GBK", dev)
                    usbCtrl.sendMsg(phoneNumber, "GBK", dev)
                    usbCtrl.sendMsg(website, "GBK", dev)

                    Command.ESC_Align[2] = 0x00.toByte()
                }else if(Retailer.connected.equals("bluetooth")){
                    Log.e("supplier name.......",reprint+"supplier..........."+supplierName)
//                    (context as? CashManActivity)?.SendDataByte(supplierName.toByteArray(), context)
                   /* val init = byteArrayOf(0x1B, 0x40) // Initialize
                    (context as? CashManActivity)?.SendDataByte(init, context)

                    val text = "Test Print\n\n"
                    (context as? CashManActivity)?.SendDataByte(text.toByteArray(charset("GBK")), context)

                    val cut = byteArrayOf(0x1D, 0x56, 0x01)
                    (context as? CashManActivity)?.SendDataByte(cut, context)*/
                    val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
                    SendBytes(alignCenter)

                    SendText("$supplierName\n")
                    SendText("$reprint\n\n")

// Align left
                    val alignLeft = byteArrayOf(0x1B, 0x61, 0x00)
                    SendBytes(alignLeft)

                    SendText("Date${getWhiteSpace(32 - 8)}Time\n")
                    SendText("$date${getWhiteSpace(32 - date.length - time.length)}$time\n")
                    SendText("Reference:${getWhiteSpace(32 - 10 - reference.length)}$reference\n")
                    SendText("Customer #:${getWhiteSpace(32 - 11 - customerNumber.length)}$customerNumber\n")
                    SendText("Shipment #:${getWhiteSpace(32 - 11 - shipment.length)}$shipment\n")
                    SendText("Driver #:${getWhiteSpace(32 - 9 - driver.length)}$driver\n")
                    SendText("Driver Cell #:${getWhiteSpace(32 - 14 - drivercell.length)}$drivercell\n")
                    SendText("Amount #:${getWhiteSpace(32 - 9 - amount.length)}$amount\n\n")

// Align center again
                    SendBytes(alignCenter)
                    SendText("$companyName\n")
                    SendText("$phoneNumber\n")
                    SendText("$website\n")
                    SendText("\n\n\n")

// Align left to reset
                    SendBytes(alignLeft)


//                    (context as? CashManActivity)?.SendDataString(companyName, context)
                } else{

                        try {

                            Command.ESC_Align[2] = 0x01.toByte()
                            usbCtrl.sendByte( Command.ESC_Align,dev)
                            usbCtrl.sendMsg(supplierName, "GBK", dev)
                            usbCtrl.sendMsg(reprint+"\n", "GBK", dev)
                            Command.ESC_Align[2] = 0x00.toByte()

                            usbCtrl.sendByte( Command.ESC_Align,dev)


                            Command.ESC_Align[2] = 1
                            (context as? CashManActivity)?.sendDataByte(Command.ESC_Align, context)


                            val Namebytes = supplierName.toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(Namebytes, context)


                            val reprintbyte = reprint.toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(reprintbyte, context)

                            Command.ESC_Align[2] = 0
                            (context as? CashManActivity)?.sendDataByte(Command.ESC_Align, context)

                            val dateTime = "Date" + getWhiteSpace(24) + "Time"
                            val dateTimebytes = dateTime.toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(dateTimebytes, context)


                            val dateTimeSol = date + getWhiteSpace((32 - date.length) - time.length) + time
                            val bytes4 = dateTimeSol.toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(bytes4, context)


                            val referenceByte = ("Reference:" + getWhiteSpace(22 - reference.length) + reference).toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(referenceByte, context)

                            val customerText = "Customer #:" + getWhiteSpace(21 - customerNumber.length) + customerNumber
                            val customerbytes = customerText.toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(customerbytes, context)


                            val shipmentByte = ("Shipment #:" + getWhiteSpace(21 - shipment.length) + shipment).toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(shipmentByte, context)


                            val DriverByte = ("Driver #:"+""+getWhiteSpace(32-9-driver.length)+""+driver).toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(DriverByte, context)

                            val DriverCellByte = ("Driver Cell #:"+""+getWhiteSpace(32-14-drivercell.length)+""+drivercell).toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(DriverCellByte, context)

                            val AmountByte = ("Amount #:"+""+getWhiteSpace(32-9-amount.length)+""+amount).toByteArray(Charsets.UTF_8)
                            (context as? CashManActivity)?.sendDataByte(AmountByte, context)


                            Command.ESC_Align[2] = 0x01.toByte()

                            (context as? CashManActivity)?.sendDataByte(Command.ESC_Align, context)
                            val companyNameByte = ("Amount #:"+""+getWhiteSpace(32-9-amount.length)+""+amount).toByteArray(Charsets.UTF_8)

                            (context as? CashManActivity)?.sendDataByte(companyNameByte, context)
                            val phoneNumberByte = ("Amount #:"+""+getWhiteSpace(32-9-amount.length)+""+amount).toByteArray(Charsets.UTF_8)

                            (context as? CashManActivity)?.sendDataByte(phoneNumberByte, context)
                            val websiteByte = ("Amount #:"+""+getWhiteSpace(32-9-amount.length)+""+amount).toByteArray(Charsets.UTF_8)

                            (context as? CashManActivity)?.sendDataByte(websiteByte, context)



                            Command.ESC_Align[2] = 0x00.toByte()
                            (context as? CashManActivity)?.sendDataByte(Command.ESC_Align, context)


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                }



            }else{
                if(Retailer.connected.equals("usb")){
                    Log.e("supplier name.......","supplier..........."+supplierName)

                    Command.ESC_Align[2] = 0x01.toByte()
                    usbCtrl.sendByte( Command.ESC_Align,dev)
                    usbCtrl.sendMsg(supplierName, "GBK", dev)
                    usbCtrl.sendMsg(reprint+"\n", "GBK", dev)
                    Command.ESC_Align[2] = 0x00.toByte()

                    usbCtrl.sendByte( Command.ESC_Align,dev)

                    usbCtrl.sendMsg("Date"+""+getWhiteSpace(32 -8)+"Time", "GBK", dev)
                    usbCtrl.sendMsg(date+""+getWhiteSpace(32-date.length-time.length)+""+time, "GBK", dev)
                    usbCtrl.sendMsg("Reference:"+""+getWhiteSpace(32-10-reference.length)+""+reference, "GBK", dev)
                    usbCtrl.sendMsg("Customer #:"+""+getWhiteSpace(32-11-customerNumber.length)+""+customerNumber, "GBK", dev)
                    usbCtrl.sendMsg("Shipment #:"+""+getWhiteSpace(32-11-shipment.length)+""+shipment, "GBK", dev)
                    usbCtrl.sendMsg("Driver #:"+""+getWhiteSpace(32-9-driver.length)+""+driver, "GBK", dev)
                    usbCtrl.sendMsg("Driver Cell #:"+""+getWhiteSpace(32-14-drivercell.length)+""+drivercell, "GBK", dev)
                    usbCtrl.sendMsg("Amount #:"+""+getWhiteSpace(32-9-amount.length)+""+amount, "GBK", dev)


                    Command.ESC_Align[2] = 0x01.toByte()
                    usbCtrl.sendByte( Command.ESC_Align,dev)
                    usbCtrl.sendMsg(companyName, "GBK", dev)
                    usbCtrl.sendMsg(phoneNumber, "GBK", dev)
                    usbCtrl.sendMsg(website, "GBK", dev)

                    Command.ESC_Align[2] = 0x00.toByte()
                }else if(Retailer.connected.equals("bluetooth")){
                    Log.e("supplier name.......",reprint+"supplier..........."+supplierName)
//                    (context as? CashManActivity)?.SendDataByte(supplierName.toByteArray(), context)
                    /* val init = byteArrayOf(0x1B, 0x40) // Initialize
                     (context as? CashManActivity)?.SendDataByte(init, context)

                     val text = "Test Print\n\n"
                     (context as? CashManActivity)?.SendDataByte(text.toByteArray(charset("GBK")), context)

                     val cut = byteArrayOf(0x1D, 0x56, 0x01)
                     (context as? CashManActivity)?.SendDataByte(cut, context)*/
                    val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
                    SendBytes(alignCenter)

                    SendText("$supplierName\n")
                    SendText("$reprint\n\n")

// Align left
                    val alignLeft = byteArrayOf(0x1B, 0x61, 0x00)
                    SendBytes(alignLeft)

                    SendText("Date${getWhiteSpace(32 - 8)}Time\n")
                    SendText("$date${getWhiteSpace(32 - date.length - time.length)}$time\n")
                    SendText("Reference:${getWhiteSpace(32 - 10 - reference.length)}$reference\n")
                    SendText("Customer #:${getWhiteSpace(32 - 11 - customerNumber.length)}$customerNumber\n")
                    SendText("Shipment #:${getWhiteSpace(32 - 11 - shipment.length)}$shipment\n")
                    SendText("Driver #:${getWhiteSpace(32 - 9 - driver.length)}$driver\n")
                    SendText("Driver Cell #:${getWhiteSpace(32 - 14 - drivercell.length)}$drivercell\n")
                    SendText("Amount #:${getWhiteSpace(32 - 9 - amount.length)}$amount\n\n")

// Align center again
                    SendBytes(alignCenter)
                    SendText("$companyName\n")
                    SendText("$phoneNumber\n")
                    SendText("$website\n")
                    SendText("\n\n\n")

// Align left to reset
                    SendBytes(alignLeft)


//                    (context as? CashManActivity)?.SendDataString(companyName, context)
                } else{

                printer?.printInit()
                printer?.let { thisPrinter ->
                    thisPrinter.clearPrintDataCache()
                    thisPrinter.printString(supplierName, 30, center, true, false)
                    if (isReprint) {
                        thisPrinter.printPaper(5)
                        thisPrinter.printString(reprint, 28, center, false, true)
                    }
                    thisPrinter.printPaper(10)
                    /* thisPrinter.printString(
                         receiptNumber, boldFont, 28, center,
                         false, false, false
                     )*/
                    thisPrinter.printPaper(15)
                    thisPrinter.print2StringInLine(
                        "Date", "Time", 1.0f, defaultFont, defaultFontSize,
                        left, false, true, false
                    )
                    thisPrinter.print2StringInLine(
                        date, time, 1.0f, boldFont, defaultFontSize,
                        left, false, false, false
                    )
                    /*  thisPrinter.print2StringInLine(
                          "Cashier", cashier, 1.0f, defaultFont,
                          defaultFontSize, left, false, false, false
                      )
                      thisPrinter.printPaper(10)

                      thisPrinter.print2StringInLine(
                          "Account #:", accountNumber, 1.0f, Printer.Font.SANS_SERIF,
                          defaultFontSize, left, false, false, false
                      )*/
                    thisPrinter.print2StringInLine(
                        "Reference:", reference, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Customer #:", customerNumber, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Shipment #:", shipment, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Driver #:", driver, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Driver Cell #:", drivercell, 1.0f, Printer.Font.SANS_SERIF,
                        defaultFontSize, left, false, false, false
                    )
                    thisPrinter.print2StringInLine(
                        "Amount:", amount, 1.0f, boldFont,
                        defaultFontSize, left, true, false, true
                    )

                    //Footer
                    thisPrinter.printPaper(20)
                    thisPrinter.printString(companyName, 25, center, false, false)
                    thisPrinter.printString(phoneNumber, 28, center, false, false)
                    thisPrinter.printString(website, 25, center, false, false)

                    //TODO Update SDK to use this function
                    //printer.printMultiseriateString()

                    thisPrinter.printPaper(80)
            } }

            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun getWhiteSpace(size: Int): String? {
        val builder = StringBuilder(size)
        for (i in 0 until size) {
            builder.append(' ')
        }
        return builder.toString()
    }
    fun SendBytes(bytes: ByteArray) {
        (context as? CashManActivity)?.SendDataByte(bytes, context)
    }

    fun SendText(text: String) {
        SendBytes(text.toByteArray(charset("GBK")))
    }
    private fun printDefaultReceiptNew(payment: String, isReprint: Boolean) {
        val defaultFontSize = context.resources.getInteger(R.integer.receipt_default_font_size)

        val companyName = context.getString(R.string.company_name)
        val phoneNumber = context.getString(R.string.company_phone)
        val reprint = context.getString(R.string.reprintLabel)
        val website = context.getString(R.string.company_website)
        if(Retailer.connected.equals("usb")){
            Command.ESC_Align[2] = 0x01.toByte()
            usbCtrl.sendByte(Command.ESC_Align, dev)

// Bold ON
            val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
            usbCtrl.sendByte(boldOn, dev)

// Print payment text
            usbCtrl.sendMsg("$payment\n", "GBK", dev)

// Bold OFF
            val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
            usbCtrl.sendByte(boldOff, dev)

            if (isReprint) {
                // Small paper feed (5 units)
                usbCtrl.sendMsg("\n", "GBK", dev)

                // Underline ON
                val underlineOn = byteArrayOf(0x1B, 0x2D, 0x01)
                usbCtrl.sendByte(underlineOn, dev)

                usbCtrl.sendMsg("$reprint\n", "GBK", dev)

                // Underline OFF
                val underlineOff = byteArrayOf(0x1B, 0x2D, 0x00)
                usbCtrl.sendByte(underlineOff, dev)
            }

// Additional line spacing
            usbCtrl.sendMsg("\n\n", "GBK", dev)       // ~10 spacing
            usbCtrl.sendMsg("\n\n\n\n", "GBK", dev)   // ~20 spacing

// Final feed to clear printer buffer or move paper forward (~80 spacing)
            usbCtrl.sendMsg("\n\n\n\n\n\n\n\n", "GBK", dev)
        }else if(Retailer.connected.equals("bluetooth")){
            val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
            SendBytes(alignCenter)

// Bold ON
            val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
            SendBytes(boldOn)

// Print main payment text
            SendText("$payment\n")

// Bold OFF
            val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
            SendBytes(boldOff)

            if (isReprint) {
                // Add 5 dot lines spacing (approx ~5mm if needed)
                SendText("\n")

                // Underline ON
                val underlineOn = byteArrayOf(0x1B, 0x2D, 0x01)
                SendBytes(underlineOn)

                SendText("$reprint\n")

                // Underline OFF
                val underlineOff = byteArrayOf(0x1B, 0x2D, 0x00)
                SendBytes(underlineOff)
            }

// Add spacing (10 and 20 units as line feeds)
            SendText("\n\n")       // ~10 spacing
            SendText("\n\n\n\n")   // ~20 spacing

// Final feed to cut area or separate
            SendText("\n\n\n\n\n\n\n\n")
        }else{
            try {
                logger(TAG, "Printing printDefaultReceipt")
                printer?.printInit()
                printer?.let { thisPrinter ->
                    thisPrinter.clearPrintDataCache()
                    thisPrinter.printString(payment, 30, center, true, false)
                    if (isReprint) {
                        thisPrinter.printPaper(5)
                        thisPrinter.printString(reprint, 28, center, false, true)
                    }
                    thisPrinter.printPaper(10)


                    //Footer
                    thisPrinter.printPaper(20)
//                thisPrinter.printString(companyName, 25, center, false, false)
//                thisPrinter.printString(phoneNumber, 28, center, false, false)
//                thisPrinter.printString(website, 25, center, false, false)

                    //TODO Update SDK to use this function
                    //printer.printMultiseriateString()

                    thisPrinter.printPaper(80)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printBitmapReceipt(bitmap: Bitmap) {
        try {
            printer?.printInit()
            printer?.clearPrintDataCache()
            printer?.printImageBase(bitmap, bitmap.width, bitmap.height, center, 0)
            printer?.printPaper(80)
        } catch (e: RemoteException){
            e.printStackTrace()
        }
    }


    private fun printReceipt(payment: Payment, bitmap: Bitmap? = null, isReprint: Boolean = false): Int {
        var result = -1
        loop = true
        logger(TAG, "Printing printReceipt 11111111")


        if(Retailer.connected.equals("usb")) {
            printDefaultReceipt(payment, isReprint)

        }else if(Retailer.connected.equals("bluetooth")){
            printDefaultReceipt(payment, isReprint)

        }else{


            val statusArray = IntArray(1)
            var status: Int = -1

            try {
                status = printer?.getPrinterStatus(statusArray) ?: -1
                logger(TAG, "Print Receipt Status: $status")
                logger(TAG, "Print Receipt StatusArray: ${statusArray[0]}")
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            if (status == 0){
                if (statusArray[0] == 138) {
                    result = statusArray[0]
                    return result
                }
            } else {
                logger(TAG, "Print Receipt: Failed to communicate with the printer.")
                return status
            }

            //todo Print two copies if not reprint
            val count  = if (isReprint) 1 else 2
            for (i in 1..count) {
                if (printerExists) {
                    if (bitmap == null) {
                        printDefaultReceipt(payment, isReprint)
                    } else {
                        printBitmapReceipt(bitmap)
                    }
                    result = printer?.printFinish() ?: -1
                }
            }

            if (result == 0) {
                loop = false
            }
        }


        return result
    }
    private fun printSupplierReceipt(payment: MyItem, bitmap: Bitmap? = null, isReprint: Boolean = false): Int {
        var result = -1
        loop = true

        logger(TAG, "Printing printReceipt 222222222")

        if(Retailer.deviceTypeTo.equals("Mobile")){
            val count = if (isReprint) 1 else 2
            for (i in 1..count) {
                    if (bitmap == null) {
                        Log.e("print","supplier report 1......")
                        printSupplierDefaultReceipt(payment, isReprint)
                    } else {
                        printBitmapReceipt(bitmap)
                    }
            }
        }else {
            val statusArray = IntArray(1)
            var status: Int = -1

            try {
                status = printer?.getPrinterStatus(statusArray) ?: -1
                logger(TAG, "Print Receipt Status: $status")
                logger(TAG, "Print Receipt StatusArray: ${statusArray[0]}")
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            if (status == 0) {
                if (statusArray[0] == 138) {
                    result = statusArray[0]
                    return result
                }
            } else {
                logger(TAG, "Print Receipt: Failed to communicate with the printer.")
                return status
            }

            //todo Print two copies if not reprint
            val count = if (isReprint) 1 else 2
            for (i in 1..count) {
                if (printerExists) {
                    if (bitmap == null) {
                        Log.e("print","supplier report 2......")

                        printSupplierDefaultReceipt(payment, isReprint)
                    } else {
                        printBitmapReceipt(bitmap)
                    }
                    result = printer?.printFinish() ?: -1
                }
            }

            if (result == 0) {
                loop = false
            }
        }
        return result
    }

    private fun printReceiptNew(payment: String, bitmap: Bitmap? = null, isReprint: Boolean = false): Int {
        var result = -1
        loop = true

        logger(TAG, "Printing printReceipt 3333333333")

        if(Retailer.connected.equals("usb")) {
            logger(TAG, "Printing usb 3333333333")

            printDefaultReceiptNew(payment, isReprint)

        }else if(Retailer.connected.equals("bluetooth")){
            logger(TAG, "Printing bluetooth 3333333333")

            printDefaultReceiptNew(payment, isReprint)
                loop = false

        }else {

            val statusArray = IntArray(1)
            var status: Int = -1

            try {
                status = printer?.getPrinterStatus(statusArray) ?: -1
                logger(TAG, "Print Receipt Status: $status")
                logger(TAG, "Print Receipt StatusArray: ${statusArray[0]}")
            } catch (e: RemoteException) {
                e.printStackTrace()
            }

            if (status == 0) {
                if (statusArray[0] == 138) {
                    result = statusArray[0]
                    return result
                }
            } else {
                logger(TAG, "Print Receipt: Failed to communicate with the printer.")
                return status
            }

            //todo Print two copies if not reprint
            val count = if (isReprint) 1 else 2
            for (i in 1..count) {
                if (printerExists) {
                    if (bitmap == null) {
                        printDefaultReceiptNew(payment, isReprint)
                    } else {
                        printBitmapReceipt(bitmap)
                    }
                    result = printer?.printFinish() ?: -1
                }
            }

            if (result == 0) {
                loop = false
            }
        }

        return result
    }

    inner class PrintReceiptNew(payment: String, bitmap: Bitmap? = null, isReprint: Boolean = false) : Thread() {
        private val _payment = payment
        private val _bitmap = bitmap
        private val _isReprint = isReprint


        override fun run() {
            threadRunning = true
            var result: Int
            do {
                /*
                try {
                    printer.printInit()
                    //clear print cache
                    printer.clearPrintDataCache()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }*/
                try {
                    logger(TAG, "Printing inner class")
                    result = printReceiptNew(_payment, _bitmap, _isReprint)

                    //print end reserve height
                    //result = printer.printPaper(100)
                    logger(TAG, "Printing Result = $result")
                    if (result == 138) {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            context.toast("Check Paper!")
                        }
                        loop = false
                        return
                    }
                    else if (result != 0)  {
                        val handler = Handler(Looper.getMainLooper())
                       /* handler.post {
                            context.toast("An error occurred while printing. Error Code: $result")
                        }*/
                        loop = false
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    loop = false

                }
            } while (loop)
            threadRunning = false
        }

    }

    inner class PrintReceipt(payment: Payment, bitmap: Bitmap? = null, isReprint: Boolean = false) : Thread() {
        private val _payment = payment
        private val _bitmap = bitmap
        private val _isReprint = isReprint

        override fun run() {
            threadRunning = true
            var result: Int
            do {
                /*
                try {
                    printer.printInit()
                    //clear print cache
                    printer.clearPrintDataCache()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }*/
                try {
                    logger(TAG, "Printing inner class")

                    result = printReceipt(_payment, _bitmap, _isReprint)

                    //print end reserve height
                    //result = printer.printPaper(100)
                    logger(TAG, "Printing Result = $result")
                    if (result == 138) {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            context.toast("Check Paper!")
                        }
                        loop = false
                        return
                    }
                    else if (result != 0)  {
                        val handler = Handler(Looper.getMainLooper())
                       /* handler.post {
                            context.toast("An error occurred while printing. Error Code: $result")
                        }*/
                        loop = false
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } while (loop)
            threadRunning = false
        }
    }

    private fun printWithUsb(context :Context):Int{
        val uInfor = Array(8) { IntArray(2) }

         usbCtrl = UsbController(context as Activity, mHandler1)
        uInfor[0][0] = 0x1CBE
        uInfor[0][1] = 0x0003
        uInfor[1][0] = 0x1CB0
        uInfor[1][1] = 0x0003
        uInfor[2][0] = 0x0483
        uInfor[2][1] = 0x5740
        uInfor[3][0] = 0x0493
        uInfor[3][1] = 0x8760
        uInfor[4][0] = 0x0416
        uInfor[4][1] = 0x5011
        uInfor[5][0] = 0x0416
        uInfor[5][1] = 0xAABB
        uInfor[6][0] = 0x1659
        uInfor[6][1] = 0x8965
        uInfor[7][0] = 0x0483
        uInfor[7][1] = 0x5741

        usbCtrl.close()
        for (i in 0 until 8) {
            Log.e("inside usb","usb pronter for........")
            dev = usbCtrl.getDev(uInfor[i][0], uInfor[i][1])
            if (dev != null) break
        }

        if (dev != null) {


            if (!usbCtrl.isHasPermission(dev)) {
//                usbCtrl.getPermission(dev)
                context.runOnUiThread {
                    Log.i(TAG, "runOnUiThread")
                    Toast.makeText(context, "Please provide usb permission", Toast.LENGTH_SHORT).show()

                }
                return 0
            } else {
                return 1

            }

        } else {
            context.runOnUiThread {
                Log.i(TAG, "runOnUiThread")
                Toast.makeText(context, "Please Connect Usb device", Toast.LENGTH_SHORT).show()

            }
            return 2


        }
    }
    @SuppressLint("HandlerLeak")
    val mHandler1: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {

            when (msg.what) {
                UsbController.USB_CONNECTED -> {

                    Toast.makeText(context, "usb connected", Toast.LENGTH_LONG)
                        .show()
                }
                else -> {

                    Toast.makeText(
                        context,
                        "usb not connected..." + msg.what,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    inner class PrintSupplierReceipt(payment: MyItem, bitmap: Bitmap? = null, isReprint: Boolean = false) : Thread() {
        private val _payment = payment
        private val _bitmap = bitmap
        private val _isReprint = isReprint

        override fun run() {
            threadRunning = true
            var result: Int
            do {
                try {
                    logger(TAG, "Printing inner class"+Retailer.deviceTypeTo)
                    if(Retailer.deviceTypeTo.equals("Mobile")){
                        Log.e("print receipt","connected print 1111111111111");
                        if(Retailer.connected.equals("usb")) {

                            var usbStatus: Int = printWithUsb(context)

                            if (usbStatus == 1) {
                                result = printSupplierReceipt(_payment, _bitmap, _isReprint)

                            }

                            return
                        }else if(Retailer.connected.equals("bluetooth")){
                            result = printSupplierReceipt(_payment, _bitmap, _isReprint)

                            return

                        }
                    }else {
                        Log.e("print receipt","connected print 22222222222222");

                        result = printSupplierReceipt(_payment, _bitmap, _isReprint)
                        //print end reserve height
                        //result = printer.printPaper(100)
//                    logger(TAG, "Printing Result = $result")

                        if (result == 138) {
                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                context.toast("Check Paper!")

                            }
                            loop = false
                            return
                        } else if (result != 0) {
                            val handler = Handler(Looper.getMainLooper())
                           /* handler.post {
                                context.toast("An error occurred while printing. Error Code: $result")
                            }*/
                            loop = false
                            return
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } while (loop)
            threadRunning = false
        }
    }

}


