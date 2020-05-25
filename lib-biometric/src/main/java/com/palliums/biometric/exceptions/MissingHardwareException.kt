package com.palliums.biometric.exceptions

/**
 * Created by elephant on 2020/5/19 15:50.
 * Copyright © 2019-2020. All rights reserved.
 * <p>
 *
 * Thrown if the device is missing biometric hardware.
 */
class MissingHardwareException : Exception("Device has no biometric hardware.")