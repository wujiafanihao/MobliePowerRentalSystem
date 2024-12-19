package util;

public class Transaction {
    /**
     * 计算实际费用（应用会员折扣）
     * @param totalCost 原始费用
     * @param userStatus 用户状态 (VIP/SVIP)
     * @return 折扣后的实际费用
     */
    public static double calculateActualCost(double totalCost, String userStatus) {
        double actualCost = totalCost;
        if ("SVIP".equals(userStatus)) {
            actualCost *= 0.5; // SVIP 5折
        } else if ("VIP".equals(userStatus)) {
            actualCost *= 0.8; // VIP 8折
        }
        return actualCost;
    }

    /**
     * 计算押金金额
     * @param userStatus 用户状态
     * @return 押金金额
     */
    public static double calculateDeposit(String userStatus) {
        boolean isVipOrSvip = "VIP".equals(userStatus) || "SVIP".equals(userStatus);
        return isVipOrSvip ? 0 : 99.0; // VIP和SVIP不需要押金，普通用户需要99元押金
    }

    /**
     * 计算归还时的余额变动
     * @param currentBalance 当前余额
     * @param deposit 押金
     * @param actualCost 实际费用
     * @return 归还后的新余额
     */
    public static double calculateReturnBalance(double currentBalance, double deposit, double actualCost) {
        return currentBalance + deposit - actualCost;
    }

    /**
     * 检查余额是否足够支付押金
     * @param balance 当前余额
     * @param requiredAmount 所需金额
     * @return 是否余额充足
     */
    public static boolean isBalanceSufficient(double balance, double requiredAmount) {
        return balance >= requiredAmount;
    }

    /**
     * 检查用户是否是VIP或SVIP
     * @param userStatus 用户状态
     * @return 是否是VIP/SVIP用户
     */
    public static boolean isVipOrSvip(String userStatus) {
        return "VIP".equals(userStatus) || "SVIP".equals(userStatus);
    }
}
