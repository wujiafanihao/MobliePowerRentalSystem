package gui.util;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class setDocumentFilter {

    /**
     * 为 JTextField 设置文档过滤器，限制输入的最大字符数
     *
     * @param textField 需要限制输入长度的 JTextField
     * @param maxLength 允许输入的最大字符数
     */
    public static void setFilter(JTextField textField, int maxLength) {
        // 获取 JTextField 的文档对象，并设置文档过滤器
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {

            /**
             * 重写 replace 方法，用于处理文本替换操作
             *
             * @param fb       FilterBypass 对象，用于绕过文档过滤器直接操作文档
             * @param offset   替换的起始位置
             * @param length   被替换的文本长度
             * @param text     替换的新文本
             * @param attrs    替换文本的属性
             * @throws BadLocationException 如果操作的位置无效，抛出此异常
             */
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                // 获取当前文档的字符数
                int currentLength = fb.getDocument().getLength();

                // 计算超出最大长度的字符数
                int overLimit = (currentLength + text.length()) - maxLength - length;

                // 如果超出最大长度，截断新输入的文本
                if (overLimit > 0) {
                    text = text.substring(0, text.length() - overLimit);
                }

                // 如果截断后的文本长度大于 0，执行替换操作
                if (text.length() > 0) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            /**
             * 重写 insertString 方法，用于处理文本插入操作
             *
             * @param fb       FilterBypass 对象，用于绕过文档过滤器直接操作文档
             * @param offset   插入的起始位置
             * @param string   插入的新文本
             * @param attr     插入文本的属性
             * @throws BadLocationException 如果操作的位置无效，抛出此异常
             */
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                // 调用 replace 方法处理插入操作
                replace(fb, offset, 0, string, attr);
            }
        });
    }
}