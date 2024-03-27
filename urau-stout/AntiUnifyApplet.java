/*
 * Copyright 2012 Alexander Baumgartner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.jku.risc.stout.urau;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import at.jku.risc.stout.urau.algo.AntiUnify;
import at.jku.risc.stout.urau.algo.AntiUnifyProblem;
import at.jku.risc.stout.urau.algo.DebugLevel;
import at.jku.risc.stout.urau.algo.RigidityFnc;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;
import at.jku.risc.stout.urau.data.NodeFactory;
import at.jku.risc.stout.urau.util.ControlledException;

public class AntiUnifyApplet extends Applet {
	private static final long serialVersionUID = -1261469987435837893L;

	private JTextArea textArea = new JTextArea();
	private JScrollPane scroll = new JScrollPane(textArea,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	private static final int MAX_LINES = 5000;
	// Jitter for MAX_LINES = 4900 - 5100
	private static final int MAX_LINES_VARIANZ = 100;
	private static final NumberFormat LINENUM_FORMAT = new DecimalFormat(
			"000000: ");
	private static final String PRINT_HEAD = "";
	private static final String PRINT_TAIL = "DONE";

	private final PrintStream out = new PrintStream(new OutputStream() {
		StringBuilder buffer = new StringBuilder();
		long count = 0L;

		@Override
		public void write(int b) throws IOException {
			buffer.appendCodePoint(b);
			if (b == '\n') {
				count++;
				flush();
			}
		}

		@Override
		public void flush() throws IOException {
			display(LINENUM_FORMAT.format(count) + buffer.toString());
			buffer.setLength(0);
		}
	});
	private final PrintStream info = new PrintStream(new OutputStream() {
		StringBuilder buffer = new StringBuilder();

		@Override
		public void write(int b) throws IOException {
			buffer.appendCodePoint(b);
			if (b == '\n')
				flush();
		}

		@Override
		public void flush() throws IOException {
			display("INFO:  " + buffer.toString());
			buffer.setLength(0);
		}
	});
	private final PrintStream err = new PrintStream(new OutputStream() {
		StringBuilder buffer = new StringBuilder();

		@Override
		public void write(int b) throws IOException {
			buffer.appendCodePoint(b);
			if (b == '\n')
				flush();
		}

		@Override
		public void flush() throws IOException {
			display("ERROR: " + buffer.toString());
			buffer.setLength(0);
		}
	});

	@Override
	public void init() {
		textArea.setEditable(false);
		scroll.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					private boolean autoAdjust = true;

					@Override
					public void adjustmentValueChanged(AdjustmentEvent e) {
						if (e.getAdjustmentType() == AdjustmentEvent.TRACK)
							autoAdjust = e.getAdjustable().getMaximum() <= e
									.getAdjustable().getValue();
						if (autoAdjust)
							e.getAdjustable().setValue(
									e.getAdjustable().getMaximum());
					}
				});
		scroll.setPreferredSize(new Dimension(getWidth(), getHeight() - 5));
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getHeight() / 20));
		add(scroll);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception isOk_UseDefaultLookAndFeel) {
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		scroll.setPreferredSize(new Dimension(width, height - 5));
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, getHeight() / 20));
	}

	@Override
	public void start() {
		display(PRINT_HEAD);
		try {
			String unifProblem = getParameter("problemset");
			if (unifProblem == null || unifProblem.length() == 0) {
				err.println("No anti-unification problem sent by the browser.\n\n"
						+ "This might be a communication-error between your browser and the Java-Applet.\n"
						+ "Please try it once more. If you use Opera, please turn off the Turbo mode.\n"
						+ "If the problem persists, please try another browser.");
			} else {
				DebugLevel debugLevel = DebugLevel.VERBOSE;
				try {
					debugLevel = DebugLevel.valueOf(getParameter("debugmode"));
				} catch (Exception ignored) {
				}
				NodeFactory.resetCounter(); // reset fresh variable counter

				boolean iterateAll = "true".equals(getParameter("iterateall"));
				RigidityFnc r = (RigidityFnc) Class.forName(
						getParameter("rigidityFnc")).newInstance();
				int minLen = 0;
				try {
					minLen = Integer.parseInt(getParameter("minlen"));
				} catch (Exception e) {
				}
				r.setMinLen(minLen);

				EquationSystem<AntiUnifyProblem> sys = new EquationSystem<AntiUnifyProblem>() {
					@Override
					public AntiUnifyProblem newEquation() {
						return new AntiUnifyProblem();
					}
				};

				info.println("Rigidity function = "
						+ getParameter("rigidityfnc"));
				info.println("Minimum alignment length = " + minLen);
				info.println("Iterate all possibilities = " + iterateAll);
				info.println("Output format = " + debugLevel);
				info.println();
				info.println("PROBLEM:");
				new InputParser<AntiUnifyProblem>(sys).parseEquationSystem(
						unifProblem, info);
				info.println();

				AntiUnify rau = new AntiUnify(r, sys, debugLevel);
				rau.antiUnify(iterateAll, out);
			}
		} catch (ControlledException ex) {
			err.println(ex.getMessage() + " (" + ex.getClass().getSimpleName()
					+ ")");
		} catch (Throwable ex) {
			ex.printStackTrace(err);
		}
		display(PRINT_TAIL);
		try {
			Thread.sleep(300);
		} catch (InterruptedException ignored) {
		}
	}

	private void display(String str) {
		try {
			if (textArea.getLineCount() >= MAX_LINES + MAX_LINES_VARIANZ) {
				textArea.replaceRange(null, 0,
						textArea.getLineEndOffset(MAX_LINES_VARIANZ * 2));
			}
			textArea.append(str);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public String getSelectedText() {
		return textArea.getSelectedText();
	}

}
