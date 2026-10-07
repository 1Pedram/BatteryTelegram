<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#0F172A"
    android:fitsSystemWindows="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <!-- Intro & Bot Link Card -->
        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="#1E293B"
            app:cardCornerRadius="16dp"
            app:strokeColor="#334155"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="⚡ Battery Reporter"
                    android:textColor="#38BDF8"
                    android:textSize="18sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Monitors your device's power levels in the background and sends automated warnings directly to your Telegram bot."
                    android:textColor="#94A3B8"
                    android:textSize="13sp" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:orientation="horizontal">

                    <Button
                        android:id="@+id/openBotButton"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_marginEnd="6dp"
                        android:layout_weight="1"
                        android:backgroundTint="#38BDF8"
                        android:text="Open Bot"
                        android:textColor="#0F172A"
                        android:textSize="12sp" />

                    <Button
                        android:id="@+id/shareBotButton"
                        style="@style/Widget.Material3.Button.TonalButton"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="6dp"
                        android:layout_weight="1"
                        android:text="Share Bot"
                        android:textColor="#FFFFFF"
                        android:textSize="12sp" />
                </LinearLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Current Battery Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/statusCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            app:cardBackgroundColor="#1E293B"
            app:cardCornerRadius="16dp"
            app:strokeColor="#334155"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Current Device Battery"
                    android:textColor="#94A3B8"
                    android:textSize="13sp" />

                <TextView
                    android:id="@+id/batteryText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="--%"
                    android:textColor="#FFFFFF"
                    android:textSize="40sp"
                    android:textStyle="bold" />

                <TextView
                    android:id="@+id/powerSourceText"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Reading status..."
                    android:textColor="#38BDF8"
                    android:textSize="15sp"
                    android:textStyle="bold" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Device Name Input -->
        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/nameLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Device Identifier Name"
            android:textColorHint="#94A3B8"
            app:boxStrokeColor="#38BDF8"
            app:hintTextColor="#38BDF8">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/deviceNameInput"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textCapWords"
                android:textColor="#FFFFFF" />
        </com.google.android.material.textfield.TextInputLayout>

        <!-- Threshold Slider (1% to 100%) -->
        <TextView
            android:id="@+id/thresholdLabel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Alert below: 20%"
            android:textColor="#CBD5E1" />

        <com.google.android.material.slider.Slider
            android:id="@+id/thresholdSlider"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:stepSize="1"
            android:value="20"
            android:valueFrom="1"
            android:valueTo="100" />

        <!-- Unlinked Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/unlinkedCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            app:cardBackgroundColor="#1E293B"
            app:cardCornerRadius="16dp"
            app:strokeColor="#334155"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Pair With Telegram"
                    android:textColor="#FFFFFF"
                    android:textSize="16sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Open the bot and tap /start to generate your 6-digit code."
                    android:textColor="#94A3B8"
                    android:textSize="13sp" />

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:hint="6-Digit Code"
                    android:textColorHint="#94A3B8"
                    app:boxStrokeColor="#38BDF8"
                    app:hintTextColor="#38BDF8">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/codeInput"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:inputType="number"
                        android:maxLength="6"
                        android:textColor="#FFFFFF" />
                </com.google.android.material.textfield.TextInputLayout>

                <ProgressBar
                    android:id="@+id/verifyProgress"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:layout_marginTop="8dp"
                    android:visibility="gone" />

                <Button
                    android:id="@+id/verifyButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:backgroundTint="#38BDF8"
                    android:text="Pair Device"
                    android:textColor="#0F172A" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Linked Card -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/linkedCard"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:visibility="gone"
            app:cardBackgroundColor="#1E293B"
            app:cardCornerRadius="16dp"
            app:strokeColor="#334155"
            app:strokeWidth="1dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="✅ Connected to Telegram"
                    android:textColor="#4ADE80"
                    android:textSize="16sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Reporting is active. Tap the test button to deliver an immediate ping to your Telegram chat."
                    android:textColor="#94A3B8"
                    android:textSize="13sp" />

                <Button
                    android:id="@+id/sendTestButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="14dp"
                    android:backgroundTint="#334155"
                    android:text="Send Immediate Test Ping"
                    android:textColor="#FFFFFF" />

                <Button
                    android:id="@+id/unlinkButton"
                    style="@style/Widget.Material3.Button.TextButton"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Disconnect This Device"
                    android:textColor="#F87171" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
