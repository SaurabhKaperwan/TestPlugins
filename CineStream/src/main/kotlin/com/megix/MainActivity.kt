package com.megix

import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the Switch by ID
        val switchButton = findViewById<Switch>(R.id.switchButton)

        // Set a listener for state changes
        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch is ON
                Toast.makeText(this, "Switch is ON", Toast.LENGTH_SHORT).show()
            } else {
                // Switch is OFF
                Toast.makeText(this, "Switch is OFF", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
