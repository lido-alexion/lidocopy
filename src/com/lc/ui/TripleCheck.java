package com.lc.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.lc.common.CopySettings;
import com.lc.common.Util;
import com.lc.core.DstExistence;
import com.sun.javafx.geom.Rectangle;

public class TripleCheck extends JPanel {

	private static final long serialVersionUID = 1L;

	public final static int CHECKED = 1;
	public final static int UNCHECKED = 0;
	public final static int MIXED = -1;

	private int state = UNCHECKED;
	private boolean mixedToUnchecked = false;
	JLabel check;
	JLabel label;
	boolean isEnabled = true;
	ImageIcon deadIcon = new ImageIcon(Util.getImageDirPath() + "dead_check.png");
	ImageIcon checkedIcon = new ImageIcon(Util.getImageDirPath() + "checked.png");
	ImageIcon uncheckedIcon = new ImageIcon(Util.getImageDirPath() + "unchecked.png");
	ImageIcon mixedIcon = new ImageIcon(Util.getImageDirPath() + "mixed_check.png");
	ImageIcon disabledCheckedIcon = new ImageIcon(Util.getImageDirPath() + "disabled_checked.png");
	ImageIcon disabledUncheckedIcon = new ImageIcon(Util.getImageDirPath() + "disabled_unchecked.png");
	ImageIcon disabledMixedIcon = new ImageIcon(Util.getImageDirPath() + "disabled_mixed_check.png");
	MouseAdapter linkAdapter;

	List<String> selectedDstList;
	List<DstExistence> allDstList;

	boolean isDead; // a dead check's state never changes
	boolean isDormant = false; // a dormant check is not fully dead, but currently inactive. So, the check
								// state is unaffected, but label is affected.
	Color fColor = Color.black;
	Color deadForeColor = new Color(0, 0, 0, 200);

	Cursor pointerCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

	private int totalCount = 0;

	public TripleCheck(String overwriteTypeKey) {
		super(null);

		this.selectedDstList = new ArrayList<String>();

		allDstList = CopySettings.overwriteTypeToFilesMap.get(overwriteTypeKey);
		totalCount = allDstList != null ? allDstList.size() : 0;

		this.check = new JLabel();
		check.setIcon(uncheckedIcon);
		check.setBounds(0, 0, 16, 16);
		check.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				changeStateOnClick();
			}

		});
		this.add(check);

		label = new JLabel();
		label.setFont(new Font("Arial", Font.PLAIN, 13));
		label.setVerticalAlignment(SwingConstants.CENTER);

		if (allDstList != null && allDstList.size() > 0) {
			label.setCursor(handCursor);
			label.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent e) {
					if (isEnabled) {
						new ExistingDstDlg(overwriteTypeKey, selectedDstList);
						selectedDstList = ExistingDstDlg.getSelectedDstPathsOverwrite();
						if (selectedDstList.size() == 0) {
							setSelected(false);
						} else if (selectedDstList.size() == totalCount) {
							setSelected(true);
						} else {
							setMixed();
						}
						updateLabelText();
					}
				}

			});
		}
		add(label);

		updateIcon();

		setForeground(Color.BLUE);
		updateLabelText();
		setDead(allDstList == null);
	}

	private void changeStateOnClick() {
		if (isEnabled() && !isDead()) {
			if (mixedToUnchecked) {
				if (state == CHECKED) {
					state = UNCHECKED;
				} else if (state == UNCHECKED) {
					state = CHECKED;
				} else if (state == MIXED) {
					state = UNCHECKED;
				}
			} else {
				if (state == CHECKED) {
					state = UNCHECKED;
				} else if (state == UNCHECKED) {
					state = CHECKED;
				} else if (state == MIXED) {
					state = CHECKED;
				}
			}

			updateIcon();
		}
		setSelectedDstListBasedOnState();
		updateLabelText();
	}

	private void setSelectedDstListBasedOnState() {
		if (state == CHECKED) {
			selectedDstList.clear();
			for (DstExistence dst : allDstList) {
				selectedDstList.add(dst.getDstPath());
			}
		} else if (state == UNCHECKED) {
			selectedDstList.clear();
		}
	}

	private void updateLabelText() {
		if (isDead) {
			setText("No such files");
		} else {
			setText("Overwrite (" + selectedDstList.size() + " of " + totalCount + ")");
		}
	}

	public void updateIcon() {
		if (check == null) {
			return;
		}
		if (isDead()) {
			check.setIcon(deadIcon);
		} else if (isEnabled()) {
			if (state == CHECKED) {
				check.setIcon(checkedIcon);
			} else if (state == MIXED) {
				check.setIcon(mixedIcon);
			} else {
				check.setIcon(uncheckedIcon);
			}
		} else {
			if (state == CHECKED) {
				check.setIcon(disabledCheckedIcon);
			} else if (state == MIXED) {
				check.setIcon(disabledMixedIcon);
			} else {
				check.setIcon(disabledUncheckedIcon);
			}
		}
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setTransitMixedToUnchecked(boolean mixedToUnchecked) {
		this.mixedToUnchecked = mixedToUnchecked;
	}

	public boolean getTransitMixedToUnchecked() {
		return this.mixedToUnchecked;
	}

	public void setSelected(boolean selected) {
		if (!isDead()) {
			this.state = selected ? CHECKED : UNCHECKED;
			updateIcon();
			setSelectedDstListBasedOnState();
			updateLabelText();
		}
	}

	public boolean isSelected() {
		return this.state == CHECKED;
	}

	public boolean isUnselected() {
		return this.state == UNCHECKED;
	}

	public boolean isMixed() {
		return this.state == MIXED;
	}

	public int getState() {
		return this.state;
	}

	public void setMixed() {
		if (!isDead()) {
			this.state = MIXED;
			updateIcon();
		}
	}

	public boolean isDead() {
		return isDead;
	}

	private void setDead(boolean isDead) {
		this.isDead = isDead;
		updateIcon();
		updateLabelColor();
		updateLabelText();
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		if (this.label != null) {
			this.label.setBounds(20, 0, width - 20, height);
		}
		if (check != null) {
			check.setBounds(0, (height - 16) / 2, 16, 16);
		}
	}

	public void setBounds(Rectangle r) {
		this.setBounds(r.x, r.y, r.width, r.height);
	}

	public void setForeground(Color c) {
		fColor = c;
		deadForeColor = new Color(100, 100, 110);
		if (this.label != null) {
			this.label.setForeground(c);
			updateLabelColor();
		}
	}

	private void setText(String text) {
		if (this.label != null) {
			this.label.setText(text);
		}
	}

	public void setFont(Font font) {
		if (this.label != null) {
			this.label.setFont(font);
		}
	}

	public JLabel getLabel() {
		return label;
	}

	public JLabel getCheck() {
		return check;
	}

	public void setTooltip(String text) {
		label.setToolTipText(text);
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
		if (!isDead()) {
			label.setCursor(isEnabled ? handCursor : pointerCursor);
		}
		updateIcon();
		updateLabelColor();
	}

	private void updateLabelColor() {
		if (this.label != null) {
			if (isDead() || isDormant()) {
				label.setForeground(Color.LIGHT_GRAY);
			} else if (isEnabled()) {
				label.setForeground(fColor);
			} else {
				label.setForeground(deadForeColor);
			}
		}
	}

	public boolean isDormant() {
		return isDormant;
	}

	public void setDormant(boolean isDormant) {
		this.isDormant = isDormant;
		updateLabelColor();
	}

	public List<String> getDstsToOverwrite() {
		return selectedDstList;
	}

	public List<String> getDstsToSkip() {
		List<String> list = new ArrayList<String>();
		if (allDstList != null) {
			for (DstExistence dst : allDstList) {
				if (!selectedDstList.contains(dst.getDstPath())) {
					list.add(dst.getDstPath());
				}
			}
		}

		return list;
	}

}
