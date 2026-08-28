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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression, tvResult;
    private StringBuilder fullExpression = new StringBuilder();
    private boolean isEvaluated = false;
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
            fullExpression.setLength(0);
            fullExpression.append(item.getResult());
            tvResult.setText(item.getResult());
            tvExpression.setText(item.getExpression());
            isEvaluated = true;
            bottomSheetDialog.dismiss();
        });
        rvHistory.setAdapter(adapter);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CalculationHistory> list = db.historyDao().getAllHistory();
            runOnUiThread(() -> adapter.setData(list));
        });

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
                if (isEvaluated) {
                    fullExpression.setLength(0);
                    tvExpression.setText("");
                    isEvaluated = false;
                }
                fullExpression.append(btn.getText().toString());
                tvResult.setText(fullExpression.toString());
            });
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            triggerHaptic(v);
            if (isEvaluated) {
                fullExpression.setLength(0);
                fullExpression.append("0");
                tvExpression.setText("");
                isEvaluated = false;
            }
            if (fullExpression.length() == 0 || isOperator(fullExpression.charAt(fullExpression.length() - 1))) {
                fullExpression.append("0.");
            } else {
                fullExpression.append(".");
            }
            tvResult.setText(fullExpression.toString());
        });

        findViewById(R.id.btnComma).setOnClickListener(v -> {
            triggerHaptic(v);
            if (fullExpression.length() > 0 && !fullExpression.toString().endsWith(",")) {
                fullExpression.append(",");
                tvResult.setText(fullExpression.toString());
                isEvaluated = false;
            }
        });
    }

    private void setupOperatorButtons() {
        int[] opIds = {R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv};

        for (int id : opIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> {
                triggerHaptic(v);
                String op = btn.getText().toString();

                if (fullExpression.length() > 0) {
                    isEvaluated = false;
                    char lastChar = fullExpression.charAt(fullExpression.length() - 1);

                    if (isOperator(lastChar)) {
                        // Replace previous operator if user taps another operator
                        fullExpression.setCharAt(fullExpression.length() - 1, op.charAt(0));
                    } else if (lastChar != ',') {
                        fullExpression.append(op);
                    }
                    tvResult.setText(fullExpression.toString());
                }
            });
        }

        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            triggerHaptic(v);
            calculateFullExpression();
        });
    }

    private void setupSpecialMathButtons() {
        // Square Root
        findViewById(R.id.btnSqrt).setOnClickListener(v -> {
            triggerHaptic(v);
            if (fullExpression.length() > 0) {
                try {
                    double val = evaluateMathString(fullExpression.toString());
                    String expr = "√(" + formatNumber(val) + ")";
                    tvExpression.setText(expr);
                    if (val < 0) {
                        tvResult.setText("Error");
                    } else {
                        double res = Math.sqrt(val);
                        String formattedRes = formatNumber(res);
                        tvResult.setText(formattedRes);
                        saveHistoryEntry(expr, formattedRes);
                        fullExpression.setLength(0);
                        fullExpression.append(formattedRes);
                    }
                    isEvaluated = true;
                } catch (Exception e) {
                    tvResult.setText("Error");
                }
            }
        });

        // Cube Root
        findViewById(R.id.btnCbrt).setOnClickListener(v -> {
            triggerHaptic(v);
            if (fullExpression.length() > 0) {
                try {
                    double val = evaluateMathString(fullExpression.toString());
                    String expr = "∛(" + formatNumber(val) + ")";
                    tvExpression.setText(expr);
                    double res = Math.cbrt(val);
                    String formattedRes = formatNumber(res);
                    tvResult.setText(formattedRes);
                    saveHistoryEntry(expr, formattedRes);
                    fullExpression.setLength(0);
                    fullExpression.append(formattedRes);
                    isEvaluated = true;
                } catch (Exception e) {
                    tvResult.setText("Error");
                }
            }
        });

        // Prime Check
        findViewById(R.id.btnPrime).setOnClickListener(v -> {
            triggerHaptic(v);
            if (fullExpression.length() > 0) {
                try {
                    long val = (long) evaluateMathString(fullExpression.toString());
                    String expr = "isPrime(" + val + ")";
                    tvExpression.setText(expr);
                    boolean prime = isPrime(val);
                    String resStr = prime ? "Prime" : "Not Prime";
                    tvResult.setText(resStr);
                    saveHistoryEntry(expr, resStr);
                    isEvaluated = true;
                } catch (Exception e) {
                    tvResult.setText("Error");
                }
            }
        });

        // GCD
        findViewById(R.id.btnGCD).setOnClickListener(v -> {
            triggerHaptic(v);
            calculateCommaSeparatedOperation("GCD");
        });

        // LCM
        findViewById(R.id.btnLCM).setOnClickListener(v -> {
            triggerHaptic(v);
            calculateCommaSeparatedOperation("LCM");
        });
    }

    private void setupControlButtons() {
        // Clear (Backspace)
        findViewById(R.id.btnC).setOnClickListener(v -> {
            triggerHaptic(v);
            if (fullExpression.length() > 0) {
                fullExpression.deleteCharAt(fullExpression.length() - 1);
                tvResult.setText(fullExpression.length() == 0 ? "0" : fullExpression.toString());
            }
        });

        // All Clear
        findViewById(R.id.btnAC).setOnClickListener(v -> {
            triggerHaptic(v);
            fullExpression.setLength(0);
            isEvaluated = false;
            tvExpression.setText("");
            tvResult.setText("0");
        });
    }

    private void calculateFullExpression() {
        if (fullExpression.length() == 0) return;

        try {
            String exprStr = fullExpression.toString();
            // Remove trailing operator if present
            if (isOperator(exprStr.charAt(exprStr.length() - 1))) {
                exprStr = exprStr.substring(0, exprStr.length() - 1);
            }

            double result = evaluateMathString(exprStr);

            if (Double.isNaN(result) || Double.isInfinite(result)) {
                tvResult.setText("Cannot divide by 0");
                isEvaluated = true;
                return;
            }

            String formattedResult = formatNumber(result);
            tvExpression.setText(exprStr + " =");
            tvResult.setText(formattedResult);

            saveHistoryEntry(exprStr, formattedResult);

            fullExpression.setLength(0);
            fullExpression.append(formattedResult);
            isEvaluated = true;
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    private void calculateCommaSeparatedOperation(String op) {
        try {
            String[] parts = fullExpression.toString().split(",");
            if (parts.length >= 2) {
                long runningResult = Long.parseLong(parts[0].trim());

                for (int i = 1; i < parts.length; i++) {
                    long nextNum = Long.parseLong(parts[i].trim());
                    if (op.equals("GCD")) {
                        runningResult = computeGCD(runningResult, nextNum);
                    } else {
                        runningResult = computeLCM(runningResult, nextNum);
                    }
                }

                String expr = op + "(" + fullExpression.toString() + ")";
                tvExpression.setText(expr + " =");

                String formattedRes = String.valueOf(runningResult);
                tvResult.setText(formattedRes);
                fullExpression.setLength(0);
                fullExpression.append(runningResult);

                saveHistoryEntry(expr, formattedRes);
                isEvaluated = true;
            }
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    // Evaluates multi-number mathematical strings like "2+3+5" or "10+5x2" (BODMAS)
    private double evaluateMathString(String expression) {
        String sanitized = expression.replace("x", "*").replace(" ", "");
        List<String> tokens = tokenize(sanitized);

        Deque<Double> values = new ArrayDeque<>();
        Deque<Character> ops = new ArrayDeque<>();

        for (String token : tokens) {
            if (token.length() == 1 && isMathOp(token.charAt(0))) {
                char op = token.charAt(0);
                while (!ops.isEmpty() && hasPrecedence(op, ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(op);
            } else {
                values.push(Double.parseDouble(token));
            }
        }

        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.isEmpty() ? 0.0 : values.pop();
    }

    private List<String> tokenize(String s) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isMathOp(c)) {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
                tokens.add(String.valueOf(c));
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) tokens.add(sb.toString());
        return tokens;
    }

    private boolean isMathOp(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean hasPrecedence(char op1, char op2) {
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
        return true;
    }

    private double applyOp(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return b == 0 ? Double.NaN : a / b;
        }
        return 0;
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == 'x' || c == '/' || c == '*';
    }

    private String formatNumber(double num) {
        if (Double.isNaN(num) || Double.isInfinite(num)) return "Error";
        if (num == (long) num) {
            return String.valueOf((long) num);
        }
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(num);
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
