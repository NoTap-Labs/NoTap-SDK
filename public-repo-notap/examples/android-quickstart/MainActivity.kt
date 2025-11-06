package com.example.notap.quickstart

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeropay.sdk.ZeroPaySDK
import com.zeropay.sdk.ZeroPayConfig
import com.zeropay.sdk.Environment
import com.zeropay.enrollment.EnrollmentManager
import com.zeropay.enrollment.EnrollmentConfig
import com.zeropay.merchant.VerificationManager
import com.zeropay.merchant.VerificationConfig

/**
 * NoTap SDK Quick Start Example
 *
 * This example demonstrates:
 * 1. SDK initialization
 * 2. User enrollment
 * 3. Payment verification
 */
class MainActivity : ComponentActivity() {

    private lateinit var noTapSDK: ZeroPaySDK
    private lateinit var enrollmentManager: EnrollmentManager
    private lateinit var verificationManager: VerificationManager

    private var userUUID: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize NoTap SDK
        initializeSDK()

        setContent {
            QuickStartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun initializeSDK() {
        // Initialize NoTap SDK
        noTapSDK = ZeroPaySDK.initialize(
            context = applicationContext,
            config = ZeroPayConfig(
                apiKey = BuildConfig.NOTAP_API_KEY,  // From local.properties
                environment = Environment.SANDBOX,
                enableBiometrics = true,
                logLevel = LogLevel.DEBUG
            )
        )

        // Initialize enrollment manager
        enrollmentManager = EnrollmentManager(
            context = this,
            sdk = noTapSDK,
            config = EnrollmentConfig(
                minimumFactors = 6,
                minimumCategories = 2
            )
        )

        // Initialize verification manager
        verificationManager = VerificationManager(
            context = this,
            sdk = noTapSDK,
            config = VerificationConfig(
                timeout = 120_000,
                requireProof = true
            )
        )

        Toast.makeText(this, "NoTap SDK initialized", Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NoTap Quick Start",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (userUUID == null) {
                // Not enrolled - show enrollment button
                EnrollmentSection()
            } else {
                // Enrolled - show verification section
                VerificationSection(userUUID!!)
            }
        }
    }

    @Composable
    fun EnrollmentSection() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("You're not enrolled yet")
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { startEnrollment() }) {
                Text("Start Enrollment")
            }
        }
    }

    @Composable
    fun VerificationSection(uuid: String) {
        var amount by remember { mutableStateOf("99.99") }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Enrolled! UUID: ${uuid.take(8)}...")

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                prefix = { Text("$") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { startVerification(amount.toDoubleOrNull() ?: 0.0) }) {
                Text("Verify Payment")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = { clearUUID() }) {
                Text("Clear UUID (Re-enroll)")
            }
        }
    }

    private fun startEnrollment() {
        enrollmentManager.startEnrollment(
            onSuccess = { result ->
                // Save UUID
                userUUID = result.uuid

                Toast.makeText(
                    this,
                    "Enrolled as ${result.alias}!",
                    Toast.LENGTH_LONG
                ).show()
            },
            onError = { error ->
                Toast.makeText(
                    this,
                    "Enrollment failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            },
            onCancelled = {
                Toast.makeText(this, "Enrollment cancelled", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun startVerification(amount: Double) {
        val uuid = userUUID ?: return

        verificationManager.startVerification(
            uuid = uuid,
            amount = amount,
            currency = "USD",
            onSuccess = { result ->
                if (result.verified) {
                    Toast.makeText(
                        this,
                        "✅ Payment verified!\nConfidence: ${(result.confidence * 100).toInt()}%",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "❌ Verification failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onError = { error ->
                Toast.makeText(
                    this,
                    "Verification error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun clearUUID() {
        userUUID = null
        Toast.makeText(this, "UUID cleared", Toast.LENGTH_SHORT).show()
    }
}
