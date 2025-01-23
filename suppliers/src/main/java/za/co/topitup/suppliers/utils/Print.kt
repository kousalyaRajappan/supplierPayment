package za.co.topitup.suppliers.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import wangpos.sdk4.libbasebinder.Printer
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.models.MyItem
import za.co.topitup.suppliers.models.Payment
import kotlin.concurrent.thread

private const val TAG = "Printer"

class Print(appContext: Context){

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
                //printer.setPrintType(0);
                //printer.setPrintPaperType(0);
                //printer.printPaper(1)
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

        try {
            logger(TAG, "Printing printDefaultReceipt")
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
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun printDefaultReceiptNew(payment: String, isReprint: Boolean) {
        val defaultFontSize = context.resources.getInteger(R.integer.receipt_default_font_size)

        val companyName = context.getString(R.string.company_name)
        val phoneNumber = context.getString(R.string.company_phone)
        val reprint = context.getString(R.string.reprintLabel)
        val website = context.getString(R.string.company_website)


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
        
        logger(TAG, "Printing printReceipt")
        
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
        return result
    }
    private fun printSupplierReceipt(payment: MyItem, bitmap: Bitmap? = null, isReprint: Boolean = false): Int {
        var result = -1
        loop = true

        logger(TAG, "Printing printReceipt")

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
        return result
    }

    private fun printReceiptNew(payment: String, bitmap: Bitmap? = null, isReprint: Boolean = false): Int {
        var result = -1
        loop = true

        logger(TAG, "Printing printReceipt")

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
                        handler.post {
                            context.toast("An error occurred while printing. Error Code: $result")
                        }
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
                        handler.post {
                            context.toast("An error occurred while printing. Error Code: $result")
                        }
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


    inner class PrintSupplierReceipt(payment: MyItem, bitmap: Bitmap? = null, isReprint: Boolean = false) : Thread() {
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
                    result = printSupplierReceipt(_payment, _bitmap, _isReprint)

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
                        handler.post {
                            context.toast("An error occurred while printing. Error Code: $result")
                        }
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

}


