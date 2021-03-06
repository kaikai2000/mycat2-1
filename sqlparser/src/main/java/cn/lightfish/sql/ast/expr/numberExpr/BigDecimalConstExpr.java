package cn.lightfish.sql.ast.expr.numberExpr;

import cn.lightfish.sql.ast.expr.ValueExpr;
import java.math.BigDecimal;

public class BigDecimalConstExpr implements ValueExpr<BigDecimal> {

  final BigDecimal value;

  public BigDecimalConstExpr(BigDecimal value) {
    this.value = value;
  }

  @Override
  public BigDecimal getValue() {
    return value;
  }

  @Override
  public Class<BigDecimal> getType() {
    return BigDecimal.class;
  }
}