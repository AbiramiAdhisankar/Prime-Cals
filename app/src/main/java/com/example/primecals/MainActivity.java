package com.example.primecals;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression, tvResult;
    private StringBuilder currentInput = new StringBuilder();
    private double firstOperand = 0.0;
    private String pendingOperation = "";
    private boolean isNewOp = true;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
        ImageButton btnHistory = findViewById(R.id.btnHistory);

        tvExpression.setText("");
        tvResult.setText("0");

        btnHistory.setOnClickListener(v -> {
            triggerHaptic(v);
            showHistoryBottomSheet();
        });

        setupNumberButtons();
        setupOperatorButtons();
        setupSpecialMathButtons();
        setupControlButtons();
    }

    private void triggerHaptic(View view) {
        view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        );
    }

    private void saveHistoryEntry(String expression, String result) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            CalculationHistory history = new CalculationHistory(expression, result, System.currentTimeMillis());
            db.historyDao().insert(history);
        });
    }

    private void showHistoryBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_history_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvHistory = sheetView.findViewById(R.id.rvHistory);
        Button btnClear = sheetView.findViewById(R.id.btnClearHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        HistoryAdapter adapter = new HistoryAdapter(item -> {
            currentInput.setLength(0);
            currentInput.append(item.getResult());
            tvResult.setText(item.getResult());
            tvExpression.setText(item.getExpression());
            isNewOp = true;
            bottomSheetDialog.dismiss();
        });
        rvHistory.setAdapter(adapter);

        // Fetch records from Room DB
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CalculationHistory> list = db.historyDao().getAllHistory();
            runOnUiThread(() -> adapter.setData(list));
        });

        // Clear history action
        btnClear.setOnClickListener(v -> {
            triggerHaptic(v);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.historyDao().clearAllHistory();
                runOnUiThread(() -> adapter.setData(new ArrayList<>()));
            });
        });

        bottomSheetDialog.show();
    }

    private void setupNumberButtons() {
        int[] numIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : numIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> {
                triggerHaptic(v);
                if (isNewOp) {
                    currentInput.setLength(0);
                    isNewOp = false;
                }
                currentInput.append(btn.getText().toString());
                tvResult.setText(currentInput.toString());
            });
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            triggerHaptic(v);
            if (isNewOp) {
                currentInput.setLength(0);
                currentInput.append("0");
                isNewOp = false;
            }
            if (!currentInput.toString().contains(".")) {
                if (currentInput.length() == 0) currentInput.append("0");
                currentInput.append(".");
                tvResult.setText(currentInput.toString());
            }
        });

        findViewById(R.id.btnComma).setOnClickListener(v -> {
            triggerHaptic(v);
            if (currentInput.length() > 0 && !currentInput.toString().endsWith(",")) {
                currentInput.append(",");
                tvResult.setText(currentInput.toString());
                isNewOp = false;
            }
        });
    }

    private void setupOperatorButtons() {
        int[] opIds = {R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv};

        for (int id : opIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> {
                triggerHaptic(v);
                if (currentInput.length() > 0) {
                    try {
                        firstOperand = Double.parseDouble(currentInput.toString());
                        pendingOperation = btn.getText().toString();
                        tvExpression.setText(formatNumber(firstOperand) + " " + pendingOperation);
                        isNewOp = true;
                    } catch (NumberFormatException ignored) {}
                }
            });
        }

        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            triggerHaptic(v);
            calculateResult();
        });
    }

    private void setupSpecialMathButtons() {
        // Square Root
        findViewById(R.id.btnSqrt).setOnClickListener(v -> {
            triggerHaptic(v);
            if (currentInput.length() > 0) {
                try {
                    double val = Double.parseDouble(currentInput.toString());
                    String expr = "√(" + formatNumber(val) + ")";
                    tvExpression.setText(expr);
                    if (val < 0) {
                        tvResult.setText("Error");
                    } else {
                        double res = Math.sqrt(val);
                        String formattedRes = formatNumber(res);
                        displayFormattedResult(res);
                        saveHistoryEntry(expr, formattedRes);
                    }
                    isNewOp = true;
                } catch (Exception e) {
                    tvResult.setText("Error");
                }
            }
        });

        // Cube Root
        findViewById(R.id.btnCbrt).setOnClickListener(v -> {
            triggerHaptic(v);
            if (currentInput.length() > 0) {
                try {
                    double val = Double.parseDouble(currentInput.toString());
                    String expr = "∛(" + formatNumber(val) + ")";
                    tvExpression.setText(expr);
                    double res = Math.cbrt(val);
                    String formattedRes = formatNumber(res);
                    displayFormattedResult(res);
                    saveHistoryEntry(expr, formattedRes);
                    isNewOp = true;
                } catch (Exception e) {
                    tvResult.setText("Error");
                }
            }
        });

        // Prime Check
        findViewById(R.id.btnPrime).setOnClickListener(v -> {
            triggerHaptic(v);
            if (currentInput.length() > 0) {
                try {
                    long val = Long.parseLong(currentInput.toString().replace(".", ""));
                    String expr = "isPrime(" + val + ")";
                    tvExpression.setText(expr);
                    boolean prime = isPrime(val);
                    String resStr = prime ? "Prime" : "Not Prime";
                    tvResult.setText(resStr);
                    saveHistoryEntry(expr, resStr);
                    isNewOp = true;
                } catch (Exception e) {
                    tvResult.setText("Error");
                }
            }
        });

        // GCD
        findViewById(R.id.btnGCD).setOnClickListener(v -> {
            triggerHaptic(v);
            if (currentInput.toString().contains(",")) {
                calculateCommaSeparatedOperation("GCD");
            } else if (currentInput.length() > 0) {
                try {
                    firstOperand = Double.parseDouble(currentInput.toString());
                    pendingOperation = "GCD";
                    tvExpression.setText("GCD(" + formatNumber(firstOperand) + ", ...)");
                    isNewOp = true;
                } catch (Exception ignored) {}
            }
        });

        // LCM
        findViewById(R.id.btnLCM).setOnClickListener(v -> {
            triggerHaptic(v);
            if (currentInput.toString().contains(",")) {
                calculateCommaSeparatedOperation("LCM");
            } else if (currentInput.length() > 0) {
                try {
                    firstOperand = Double.parseDouble(currentInput.toString());
                    pendingOperation = "LCM";
                    tvExpression.setText("LCM(" + formatNumber(firstOperand) + ", ...)");
                    isNewOp = true;
                } catch (Exception ignored) {}
            }
        });
    }

    private void setupControlButtons() {
        // Clear (Backspace)
        findViewById(R.id.btnC).setOnClickListener(v -> {
            triggerHaptic(v);
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
                tvResult.setText(currentInput.length() == 0 ? "0" : currentInput.toString());
            }
        });

        // All Clear
        findViewById(R.id.btnAC).setOnClickListener(v -> {
            triggerHaptic(v);
            currentInput.setLength(0);
            firstOperand = 0.0;
            pendingOperation = "";
            isNewOp = true;
            tvExpression.setText("");
            tvResult.setText("0");
        });
    }

    private void calculateResult() {
        if (pendingOperation.isEmpty() || currentInput.length() == 0) return;

        try {
            double secondOperand = Double.parseDouble(currentInput.toString());
            double result = 0.0;
            String expressionText = formatNumber(firstOperand) + " " + pendingOperation + " " + formatNumber(secondOperand);

            tvExpression.setText(expressionText + " =");

            switch (pendingOperation) {
                case "+":
                    result = firstOperand + secondOperand;
                    break;
                case "-":
                    result = firstOperand - secondOperand;
                    break;
                case "x":
                    result = firstOperand * secondOperand;
                    break;
                case "/":
                    if (secondOperand == 0) {
                        tvResult.setText("Cannot divide by 0");
                        isNewOp = true;
                        return;
                    }
                    result = firstOperand / secondOperand;
                    break;
                case "GCD":
                    result = computeGCD((long) firstOperand, (long) secondOperand);
                    break;
                case "LCM":
                    result = computeLCM((long) firstOperand, (long) secondOperand);
                    break;
            }

            String formattedResult = formatNumber(result);
            displayFormattedResult(result);
            saveHistoryEntry(expressionText, formattedResult);

            pendingOperation = "";
            isNewOp = true;
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    private void calculateCommaSeparatedOperation(String op) {
        try {
            String[] parts = currentInput.toString().split(",");
            if (parts.length == 2) {
                long a = Long.parseLong(parts[0].trim());
                long b = Long.parseLong(parts[1].trim());
                String expr = op + "(" + a + ", " + b + ")";
                tvExpression.setText(expr + " =");
                long res = op.equals("GCD") ? computeGCD(a, b) : computeLCM(a, b);
                String formattedRes = String.valueOf(res);
                tvResult.setText(formattedRes);
                currentInput.setLength(0);
                currentInput.append(res);
                saveHistoryEntry(expr, formattedRes);
                isNewOp = true;
            }
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    private String formatNumber(double num) {
        if (num == (long) num) {
            return String.valueOf((long) num);
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(num);
    }

    private void displayFormattedResult(double result) {
        String formatted = formatNumber(result);
        tvResult.setText(formatted);
        currentInput.setLength(0);
        currentInput.append(formatted);
    }

    private boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    private long computeGCD(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private long computeLCM(long a, long b) {
        if (a == 0 || b == 0) return 0;
        return Math.abs(a * b) / computeGCD(a, b);
    }
}