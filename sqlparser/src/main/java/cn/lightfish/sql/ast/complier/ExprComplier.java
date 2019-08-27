package cn.lightfish.sql.ast.complier;

import cn.lightfish.sql.ast.SQLTypeMap;
import cn.lightfish.sql.ast.converter.Converters;
import cn.lightfish.sql.ast.expr.ValueExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.bigDecimalOperator.BigDecimalAddExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.bigDecimalOperator.BigDecimalDivisionExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.bigDecimalOperator.BigDecimalMultipyExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.bigDecimalOperator.BigDecimalSubtractExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.doubleOperator.DoubleAddExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.doubleOperator.DoubleDivisionExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.doubleOperator.DoubleMultipyExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.doubleOperator.DoubleSubtractExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.longOperator.LongAddExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.longOperator.LongDivisionExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.longOperator.LongMultipyExpr;
import cn.lightfish.sql.ast.expr.arithmeticExpr.longOperator.LongSubtractExpr;
import cn.lightfish.sql.ast.expr.booleanExpr.BooleanExistsExpr;
import cn.lightfish.sql.ast.expr.booleanExpr.BooleanExpr;
import cn.lightfish.sql.ast.expr.booleanExpr.compareExpr.BooleanEqualityExpr;
import cn.lightfish.sql.ast.expr.booleanExpr.compareExpr.BooleanLessThanExpr;
import cn.lightfish.sql.ast.expr.booleanExpr.compareExpr.BooleanNotEqualityExpr;
import cn.lightfish.sql.ast.expr.booleanExpr.logicalExpr.BooleanAndExpr;
import cn.lightfish.sql.ast.expr.booleanExpr.logicalExpr.BooleanOrExpr;
import cn.lightfish.sql.ast.expr.dateExpr.DateExpr;
import cn.lightfish.sql.ast.expr.functionExpr.FunctionManager;
import cn.lightfish.sql.ast.expr.numberExpr.BigDecimalExpr;
import cn.lightfish.sql.ast.expr.numberExpr.DoubleExpr;
import cn.lightfish.sql.ast.expr.numberExpr.LongExpr;
import cn.lightfish.sql.ast.expr.stringExpr.StringConstExpr;
import cn.lightfish.sql.ast.expr.stringExpr.StringExpr;
import cn.lightfish.sql.ast.expr.valueExpr.NullConstExpr;
import cn.lightfish.sql.context.RootSessionContext;
import cn.lightfish.sql.executor.logicExecutor.Executor;
import com.alibaba.fastsql.sql.ast.SQLExpr;
import com.alibaba.fastsql.sql.ast.SQLName;
import com.alibaba.fastsql.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.fastsql.sql.ast.expr.SQLCastExpr;
import com.alibaba.fastsql.sql.ast.expr.SQLExistsExpr;
import com.alibaba.fastsql.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.fastsql.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.fastsql.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.fastsql.sql.ast.expr.SQLValuableExpr;
import com.alibaba.fastsql.sql.ast.expr.SQLVariantRefExpr;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ExprComplier {

  final ComplierContext complieContext;
  final RootSessionContext context;

  public ExprComplier(ComplierContext context) {
    this.complieContext = context;
    this.context = context.runtimeContext;
  }

  public ValueExpr createExpr(SQLExpr sqlExpr) {
    if (sqlExpr instanceof SQLBinaryOpExpr) {
      SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) sqlExpr;
      ValueExpr leftExpr = createExpr(binaryOpExpr.getLeft());
      ValueExpr rightExpr = createExpr(binaryOpExpr.getRight());

      Class leftExprType = leftExpr.getType();
      Class rightExprType = rightExpr.getType();
      if (leftExprType != rightExprType) {
        if ((Number.class.isAssignableFrom(leftExprType) && (Number.class
            .isAssignableFrom(rightExprType)))) {
          leftExpr = createCast(leftExpr, Double.class);
          rightExpr = createCast(rightExpr, Double.class);
        }
      }
      checkReturnType(leftExpr, rightExpr, leftExpr.getType(), sqlExpr);
      switch (binaryOpExpr.getOperator()) {
        case BooleanOr:
          checkReturnType(leftExpr, rightExpr, Boolean.class, sqlExpr);
          return new BooleanOrExpr(context, (BooleanExpr) leftExpr, (BooleanExpr) rightExpr);
        case BooleanAnd:
          checkReturnType(leftExpr, rightExpr, Boolean.class, sqlExpr);
          return new BooleanAndExpr(context, (BooleanExpr) leftExpr, (BooleanExpr) rightExpr);
        case Equality:
          return new BooleanEqualityExpr(context, leftExpr, rightExpr);
        case LessThanOrGreater:
          return new BooleanNotEqualityExpr(context, leftExpr, rightExpr);
        case LessThan:
          return new BooleanLessThanExpr(context, leftExpr, rightExpr);
        case LessThanOrEqual:
          return new BooleanEqualityExpr(context, leftExpr, rightExpr);
        case GreaterThan:
          return new BooleanEqualityExpr(context, leftExpr, rightExpr);
        case GreaterThanOrEqual:
          checkReturnType(leftExpr, rightExpr, leftExpr.getType(), sqlExpr);
          return new BooleanEqualityExpr(context, leftExpr, rightExpr);
        case Add:
          if (leftExpr.getType() == Long.class) {
            return new LongAddExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == Double.class) {
            return new DoubleAddExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == BigDecimal.class) {
            return new BigDecimalAddExpr(context, leftExpr, rightExpr);
          }
          throw new UnsupportedOperationException();
        case Subtract:
          if (leftExpr.getType() == Long.class) {
            return new LongSubtractExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == Double.class) {
            return new DoubleSubtractExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == BigDecimal.class) {
            return new BigDecimalSubtractExpr(context, leftExpr, rightExpr);
          }
          throw new UnsupportedOperationException();
        case Multiply:
          if (leftExpr.getType() == Long.class) {
            return new LongMultipyExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == Double.class) {
            return new DoubleMultipyExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == BigDecimal.class) {
            return new BigDecimalMultipyExpr(context, leftExpr, rightExpr);
          }
          throw new UnsupportedOperationException();
        case Divide:
          if (leftExpr.getType() == Long.class) {
            return new LongDivisionExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == Double.class) {
            return new DoubleDivisionExpr(context, leftExpr, rightExpr);
          }
          if (leftExpr.getType() == BigDecimal.class) {
            return new BigDecimalDivisionExpr(context, leftExpr, rightExpr);
          }
          throw new UnsupportedOperationException();
        default:
      }
    } else if (sqlExpr instanceof SQLIdentifierExpr) {
      SQLIdentifierExpr expr = (SQLIdentifierExpr) sqlExpr;
      return complieContext.getColumnAllocatior().getFieldExecutor(expr.getResolvedColumn());
    } else if (sqlExpr instanceof SQLPropertyExpr) {
      SQLPropertyExpr expr = (SQLPropertyExpr) sqlExpr;
      return complieContext.getColumnAllocatior().getFieldExecutor(expr.getResolvedColumn());
    } else if (sqlExpr instanceof SQLName) {
      throw new UnsupportedOperationException();
    } else if (sqlExpr instanceof SQLValuableExpr) {
      return Converters.transfor((SQLValuableExpr) sqlExpr);
    } else if (sqlExpr instanceof SQLVariantRefExpr) {
      SQLVariantRefExpr variantRefExpr = (SQLVariantRefExpr) sqlExpr;
      return createVariantRef(variantRefExpr);
    } else if (sqlExpr instanceof SQLMethodInvokeExpr) {
      return createMethod((SQLMethodInvokeExpr) sqlExpr);
    } else if (sqlExpr instanceof SQLCastExpr) {
      SQLCastExpr expr = (SQLCastExpr) sqlExpr;
      ValueExpr value = createExpr(expr.getExpr());
      return createCast(value, SQLTypeMap.toClass(expr.getDataType().jdbcType()));
    } else if (sqlExpr instanceof SQLExistsExpr) {
      SQLExistsExpr existsExpr = (SQLExistsExpr) sqlExpr;
      return createSQLExistsExpr(existsExpr);
    }
    throw new UnsupportedOperationException();
  }

  public BooleanExpr createSQLExistsExpr(SQLExistsExpr existsExpr) {
    Executor subQuery = complieContext.getSubQueryComplier()
        .createSubQuery(existsExpr.getSubQuery().getQueryBlock(), SubQueryType.EXISTS);
    return new BooleanExistsExpr(subQuery, existsExpr.isNot());
  }



  public ValueExpr createCast(ValueExpr value, Class<?> targetType) {
    Objects.requireNonNull(targetType);
    Class orginClass = value.getType();
    if (orginClass == targetType) {
      return value;
    } else if (targetType.equals(String.class)) {
      return (StringExpr) () -> {
        Comparable v = value.getValue();
        return v == null ? null : v.toString();
      };
    } else if (targetType.equals(Long.class)) {
      if (Number.class.isAssignableFrom(orginClass)) {
        return (LongExpr) () -> {
          Number v = (Number) value.getValue();
          return v == null ? null : v.longValue();
        };
      } else if (String.class == orginClass) {
        return (LongExpr) () -> {
          String v = (String) value.getValue();
          return v == null ? null : Long.parseLong(v);
        };
      }
    } else if (targetType.equals(Double.class)) {
      if (Number.class.isAssignableFrom(orginClass)) {
        return (DoubleExpr) () -> {
          Number v = (Number) value.getValue();
          return v == null ? null : v.doubleValue();
        };
      } else if (String.class == orginClass) {
        return (DoubleExpr) () -> {
          String v = (String) value.getValue();
          return v == null ? null : Double.parseDouble(v);
        };
      }
    } else if (targetType.equals(BigDecimal.class)) {
      if (orginClass == Long.class) {
        return (BigDecimalExpr) () -> {
          Long v = (Long) value.getValue();
          return v == null ? null : new BigDecimal(v);
        };
      } else if (orginClass == Double.class) {
        return (BigDecimalExpr) () -> {
          Double v = (Double) value.getValue();
          return v == null ? null : new BigDecimal(v);
        };
      } else if (orginClass == String.class) {
        return (BigDecimalExpr) () -> {
          String v = (String) value.getValue();
          return v == null ? null : new BigDecimal(v);
        };
      }
    } else if (targetType.equals(Date.class) || targetType == java.util.Date.class) {
      if (orginClass == String.class) {
        return (DateExpr) () -> {
          String v = (String) value.getValue();
          return v == null ? null : Date.valueOf(v);
        };
      }
    } else if (targetType.equals(Timestamp.class)) {
      if (orginClass == String.class) {
        return (DateExpr) () -> {
          String v = (String) value.getValue();
          return v == null ? null : Timestamp.valueOf(v);
        };
      }
    } else if (targetType.equals(Time.class)) {
      if (orginClass == String.class) {
        return (DateExpr) () -> {
          String v = (String) value.getValue();
          return v == null ? null : Time.valueOf(v);
        };
      }
    }
    return NullConstExpr.INSTANCE;
  }

  public ValueExpr createVariantRef(SQLVariantRefExpr variantRefExpr) {
    if (variantRefExpr.isGlobal()) {
      return new StringConstExpr((String) context.getGlobalVariant(variantRefExpr.getName()));
    } else if (variantRefExpr.isSession()) {
      return new StringConstExpr((String) context.getSessionVariant(variantRefExpr.getName()));
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public ValueExpr createMethod(SQLMethodInvokeExpr methodInvokeExpr) {
    String methodName = methodInvokeExpr.getMethodName();
    Function function = FunctionManager.INSTANCE.getFunctionByName(methodName);
    Objects.requireNonNull(function);
    List<SQLExpr> arguments = methodInvokeExpr.getArguments();
    Object[] args;
    if (arguments == null || arguments.isEmpty()) {
      args = new Object[]{};
    } else {
      args = new Object[arguments.size()];
      for (int i = 0; i < args.length; i++) {
        args[i] = createExpr(arguments.get(i)).getValue();
      }
    }
    return Converters.transfor(function.apply(args));
  }

  public void checkReturnType(ValueExpr leftExpr, ValueExpr rightExpr,
      Class clazz, SQLExpr sqlExpr) {
    if (leftExpr.getType().equals(clazz) && clazz.equals(rightExpr.getType())) {

    } else {
      throw new ClassCastException(MessageFormat
          .format("{0} left is {1} right is {2}", sqlExpr, leftExpr.getType(),
              rightExpr.getType()));
    }
  }

}