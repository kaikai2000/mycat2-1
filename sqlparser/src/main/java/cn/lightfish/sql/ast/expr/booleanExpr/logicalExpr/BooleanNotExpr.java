package cn.lightfish.sql.ast.expr.booleanExpr.logicalExpr;

import cn.lightfish.sql.context.RootSessionContext;
import cn.lightfish.sql.ast.expr.booleanExpr.BooleanExpr;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BooleanNotExpr implements BooleanExpr {

  private final RootSessionContext context;
  private final BooleanExpr value;

  @Override
  public Boolean test() {
    Boolean value = this.value.test();
    if (value == null) {
      return false;
    }
    return !value;
  }
}