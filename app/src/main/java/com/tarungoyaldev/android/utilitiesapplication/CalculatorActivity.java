package com.tarungoyaldev.android.utilitiesapplication;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.tarungoyaldev.android.utilitiesapplication.tools.StringObservable;
import com.tarungoyaldev.android.utilitiesapplication.tools.SwipeDetector;

import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

public class CalculatorActivity extends AppCompatActivity {

    private Stack<String> calculationStack = new Stack<>();
    private StringObservable displayStringObserver = new StringObservable("");
    private Operation currentOperation = Operation.NULL;
    // Temporary stack and temporary string for moving back to previous state. These are used when
    // an operation is changed.
    private Stack<String> temporaryStack = new Stack<>();
    private String temporaryString = "";
    private boolean isLastOperation = false;

    public enum Operation {
        ADDITION ("ADD"),
        SUBTRACTION ("SUB"),
        MULTIPLICATION ("MUL"),
        DIVISION ("DIV"),
        EQUAL ("EQL"),
        NULL("NULL");


        private String value;

        private Operation(String value) {
            this.value = value;
        }

        boolean isLowerPrecedence(Operation otherValue) {
            if (this.equals(NULL) || this.equals(EQUAL)) {
                return true;
            } else if ((this.equals(ADDITION) || this.equals(SUBTRACTION))
                    && (otherValue.equals(MULTIPLICATION) || otherValue.equals(DIVISION))) {
                return true;
            }
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        final TextView textView = (TextView) findViewById(R.id.textView);
        assert textView != null;
        textView.setOnTouchListener(new SwipeDetector(new SwipeDetector.Callback() {
            @Override
            public boolean onLeftSwipe() {
                clearSingleDigit();
                return true;
            }
            @Override
            public boolean onRightSwipe() {
                clearSingleDigit();
                return true;
            }
        }));
        calculationStack.push(Operation.NULL.name());
        // Add observer to displayString. This observer updates the text in textView and change the
        // state of ClearButton according to the current text
        displayStringObserver.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                String text = (String) data;
                textView.setText(text);
                // Clear button clears the current number to 0 if there is text that is being
                // written. Otherwise it resets the whole calculation stack.
                Button clearButton = (Button) findViewById(R.id.clearButton);
                assert clearButton != null;
                if (text.equals("0")) {
                    clearButton.setText("AC");
                } else {
                    clearButton.setText("C");
                }
            }
        });
        // Support for setting layout_columnWeight and layout_rowWeight was added in android L and
        // above. This code handles the buttons layout in Kitkat and below.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final GridLayout gridLayout = (GridLayout) findViewById(R.id.calculatorLayout);
            gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            for (int i = 0; i < gridLayout.getChildCount(); i++) {
                                View view = gridLayout.getChildAt(i);
                                GridLayout.LayoutParams params = (GridLayout.LayoutParams) view.getLayoutParams();
                                params.width = (gridLayout.getWidth()/gridLayout.getColumnCount()) -params.rightMargin - params.leftMargin;
                                params.height = (gridLayout.getHeight()/gridLayout.getRowCount()) -params.topMargin - params.bottomMargin;
                                view.setLayoutParams(params);
                            }
                        }
            });
        }
    }

    /**
     * Handles click event on number buttons. This updates the number string displayed in calculator
     * according to the number button pressed.
     * @param view number button clicked
     */
    public void onNumberClick(View view) {
        if (view instanceof Button) {
            Button numberButton = (Button) view;
            String numberString = (String) numberButton.getTag();
            if (displayStringObserver.getObservedString().equals("0")) {
                displayStringObserver.updateString(numberString);
            } else if (numberString.equals(".")
                    && displayStringObserver.getObservedString().contains(".")) {
                return;
            } else {
                displayStringObserver.concat(numberString);
            }
        }
        isLastOperation = false;
    }

    /**
     * Handles different operations on the numbers. If the operation is changed by user, this
     * function makes use of temporaryStack and temporaryString to move back the state by one
     * operation
     * @param view operation button clicked.
     */
    public void onOperationClick(View view) {
        int viewId = view.getId();
        if (!isLastOperation) {
            temporaryStack.removeAllElements();
            temporaryStack.addAll(calculationStack);
            temporaryString = displayStringObserver.getObservedString();
        }
        if (view instanceof Button) {
            Operation buttonOperation;
            switch (viewId) {
                case R.id.additionButton:
                    buttonOperation = Operation.ADDITION;
                    break;
                case R.id.subtractionButton:
                    buttonOperation = Operation.SUBTRACTION;
                    break;
                case R.id.multiplicationButton:
                    buttonOperation = Operation.MULTIPLICATION;
                    break;
                case R.id.divisionButton:
                    buttonOperation = Operation.DIVISION;
                    break;
                case R.id.equalButton:
                    buttonOperation = Operation.EQUAL;
                    break;
                default:
                    buttonOperation = Operation.NULL;
                    break;
            }
            if (isLastOperation) {
                calculationStack.removeAllElements();
                calculationStack.addAll(temporaryStack);
                displayStringObserver.updateString(temporaryString);
            }
            applyOperation(buttonOperation);
        }
        if (viewId != R.id.equalButton) isLastOperation = true;
    }

    private void applyOperation(Operation operation) {
        currentOperation = Operation.valueOf(calculationStack.peek());
        if (operation.equals(Operation.EQUAL)) {
            while (!calculationStack.peek().equals(Operation.NULL.name())) {
                Double currentValue = Double.valueOf(displayStringObserver.getObservedString());
                Operation prevOperation = Operation.valueOf(calculationStack.pop());
                double prevValue = Double.valueOf(calculationStack.pop());
                displayStringObserver.updateString(
                        String.valueOf(operate(prevOperation,prevValue,currentValue)));
            }
            return;
        } else if (currentOperation.isLowerPrecedence(operation)) {
            calculationStack.push(displayStringObserver.getObservedString());
            if (!operation.equals(Operation.NULL)) {
                displayStringObserver.updateString("0", false);
                calculationStack.push(operation.name());
            } else {
                calculationStack.removeAllElements();
                calculationStack.push(Operation.NULL.name());
            }
        } else {
            double displayValue = Double.valueOf(displayStringObserver.getObservedString());
            Operation previousOperation = Operation.valueOf(calculationStack.pop());
            double previousValue = Double.valueOf(calculationStack.pop());
            String newValue =
                    String.valueOf(operate(previousOperation, previousValue, displayValue));
            if (newValue.endsWith(".0")) {
                newValue = newValue.substring(0, newValue.length() - 2);
            }
            displayStringObserver.updateString(newValue);
            applyOperation(operation);
        }
    }

    private String operate(Operation operation, double firstValue, double secondValue) {
        double result;
        switch (operation) {
            case ADDITION:
                result = firstValue + secondValue;
                break;
            case SUBTRACTION:
                result = firstValue - secondValue;
                break;
            case MULTIPLICATION:
                result = firstValue * secondValue;
                break;
            case DIVISION:
                result = firstValue / secondValue;
                break;
            case EQUAL:
            default:
                result = secondValue;
                break;
        }
        String resultString = String.valueOf(result);
        if (resultString.endsWith(".0")) {
            resultString = resultString.substring(0, resultString.length() - 2);
        }
        return resultString;
    }

    public void onSpecialOperationClick(View view) {
        if (view instanceof Button) {
            int viewId = view.getId();
            Button button = (Button) findViewById(viewId);
            String displayString = displayStringObserver.getObservedString();
            switch (viewId) {
                case R.id.clearButton:
                    if (button.getText().equals("AC")) {
                        calculationStack.removeAllElements();
                        calculationStack.push(Operation.NULL.name());
                    }
                    displayStringObserver.updateString("0");
                    break;
                case R.id.changeSizeButton:
                    if (displayString.charAt(0) == '-') {
                        displayStringObserver.updateString(displayString.substring(1));
                    } else {
                        displayStringObserver.updateString("-".concat(displayString));
                    }
                    break;
                case R.id.percentageButton:
                    double textValue = Double.valueOf(displayString);
                    textValue/=100;
                    String resultString = String.valueOf(textValue);
                    if (resultString.endsWith(".0")) {
                        resultString = resultString.substring(0, resultString.length() - 2);
                    }
                    displayStringObserver.updateString(resultString);
                    break;
            }
        }
        isLastOperation = false;
    }

    private void clearSingleDigit() {
        String displayString = displayStringObserver.getObservedString();
        int displayStringLen = displayString.length();
        if (displayString.length() == 1 ||
                (displayString.charAt(0) == '-' && displayStringLen == 2)) {
            displayStringObserver.updateString("0");
        } else {
            displayStringObserver.updateString(displayString.substring(0, displayStringLen - 1));
        }
    }
}
