package com.vedovatto.setslip.e2e.support;

import static com.vedovatto.setslip.e2e.support.Eventually.eventually;
import static org.assertj.core.api.Assertions.assertThat;

import com.vedovatto.setslip.e2e.screens.PrinterScreen;
import com.vedovatto.setslip.e2e.screens.Slip;
import java.util.List;

public class SlipPrinting {

    private final PrinterScreen printer;
    private final Slip slip;
    private final Memory memory;

    public SlipPrinting(PrinterScreen printer, Slip slip, Memory memory) {
        this.printer = printer;
        this.slip = slip;
        this.memory = memory;
    }

    public void print(List<Group> groups) {
        printer.selectOnly(groups);
        Slip.Printed before = slip.printed();
        printer.pressPrint();
        waitForSlipAfter(before);
    }

    public void printAnother() {
        Slip.Printed before = slip.printed();
        printer.pressAnother();
        waitForSlipAfter(before);
    }

    public void dismissTip() {
        printer.dismissTip();
        memory.rememberTipDismissed();
    }

    private void waitForSlipAfter(Slip.Printed before) {
        eventually(
                () -> {
                    assertThat(printer.canPrintAnother()).as("printer idle").isTrue();
                    assertThat(slip.printed()).as("slip on the printer").isNotEqualTo(before);
                    assertThat(printer.canPrintAnother()).as("slip finished printing").isTrue();
                });
        if (!memory.tipDismissed()) {
            dismissTip();
        }
    }
}
