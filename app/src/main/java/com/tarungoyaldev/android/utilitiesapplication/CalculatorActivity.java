package com.tarungoyaldev.android.utilitiesapplication;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.tarungoyaldev.android.utilitiesapplication.tools.StringObservable;

import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

public class CalculatorActivity extends AppCompatActivity {

    private Stack<String> calculationStack = new Stack<>();
    private StringObservable displayStringObserver = new StringObservable("");
    private Operation currentOperation = Operation.NULL;
    private Stack<String> temporaryStack = new Stack<>();
    private String temporaryString = "";
    private boolean isLastOperation = false;

    public enum Operation {
        ADDITION ("ADD"),
        SUBTRACTION ("SUB"),
        MULTIPLICATION ("MUL"),
        DIVISION ("DIV"),
        NULL("NULL");


        private String value;

        private Operation(String value) {
            this.value = value;
        }

        boolean isLowerPrecedence(Operation otherValue) {
            if (this.equals(NULL)) {
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
        textView.setText("0");
        calculationStack.push(Operation.NULL.name());
        displayStringObserver.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                String text = (String) data;
                textView.setText(text);
                Button clearButton = (Button) findViewById(R.id.clearButton);
                if (text.equals("0")) {
                    clearButton.setText("AC");
                } else {
                    clearButton.setText("C");
                }
            }
        });
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
                default:
                    buttonOperation = Operation.NULL;
                    break;
            }
            applyOperation(buttonOperation);
        }
        if (viewId != R.id.equalButton) isLastOperation = true;
    }

    private void applyOperation(Operation operation) {
        if (isLastOperation) {
            calculationStack.removeAllElements();
            calculationStack.addAll(temporaryStack);
            displayStringObserver.updateString(temporaryString);
        }
        currentOperation = Operation.valueOf(calculationStack.peek());
        if (currentOperation.isLowerPrecedence(operation)) {
            calculationStack.push(displayStringObserver.getObservedString());
            if (!operation.equals(Operation.NULL)) {
                displayStringObserver.updateString("0", false);
                calculationStack.push(operation.name());
            } else {
                calculationStack.removeAllElements();
                calculationStack.push(Operation.NULL.name());
            }
        } else {
            float displayValue = Float.valueOf(displayStringObserver.getObservedString());
            Operation previousOperation = Operation.valueOf(calculationStack.pop());
            float previousValue = Float.valueOf(calculationStack.pop());
            String newValue =
                    String.valueOf(operate(previousOperation, previousValue, displayValue));
            displayStringObserver.updateString(newValue);
            applyOperation(operation);
        }
    }

    private String operate(Operation operation, float firstValue, float secondValue) {
        float result;
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
            default:
                result = secondValue;
                break;
        }
        return String.valueOf(result);
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
                    float textValue = Float.valueOf(displayString);
                    textValue/=100;
                    displayStringObserver.updateString(String.valueOf(textValue));
                    break;
            }
        }
        isLastOperation = false;
    }
}
