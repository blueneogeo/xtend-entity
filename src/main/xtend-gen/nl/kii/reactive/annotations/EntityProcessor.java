package nl.kii.reactive.annotations;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.kii.observe.Observable;
import nl.kii.reactive.Change;
import nl.kii.reactive.EntityList;
import nl.kii.reactive.EntityMap;
import nl.kii.reactive.Reactive;
import nl.kii.reactive.ReactiveObject;
import nl.kii.reactive.annotations.Ignore;
import nl.kii.reactive.annotations.Require;
import nl.kii.util.IterableExtensions;
import nl.kii.util.OptExtensions;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.TransformationParticipant;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.CompilationStrategy;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableConstructorDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableExecutableDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class EntityProcessor implements TransformationParticipant<MutableClassDeclaration> {
  public void doTransform(final List<? extends MutableClassDeclaration> classes, @Extension final TransformationContext context) {
    final TypeReference reactiveType = context.newTypeReference(Reactive.class);
    TypeReference _newTypeReference = context.newTypeReference(Change.class);
    final TypeReference changeHandlerType = context.newTypeReference(Procedure1.class, _newTypeReference);
    final TypeReference stopObservingType = context.newTypeReference(Procedure0.class);
    Map<String, Class<? extends Object>> _xsetliteral = null;
    Map<String, Class<? extends Object>> _tempMap = Maps.<String, Class<? extends Object>>newHashMap();
    _tempMap.put("boolean", Boolean.class);
    _tempMap.put("int", Integer.class);
    _tempMap.put("long", Long.class);
    _tempMap.put("float", Float.class);
    _tempMap.put("double", Double.class);
    _xsetliteral = Collections.<String, Class<? extends Object>>unmodifiableMap(_tempMap);
    final Map<String, Class<? extends Object>> typeConversions = _xsetliteral;
    for (final MutableClassDeclaration cls : classes) {
      {
        final TypeReference clsType = context.newTypeReference(cls);
        TypeReference _newTypeReference_1 = context.newTypeReference(ReactiveObject.class);
        cls.setExtendedClass(_newTypeReference_1);
        TypeReference _newTypeReference_2 = context.newTypeReference(Cloneable.class);
        cls.setImplementedInterfaces(Collections.<TypeReference>unmodifiableList(Lists.<TypeReference>newArrayList(_newTypeReference_2)));
        Iterable<? extends MutableFieldDeclaration> _declaredFields = cls.getDeclaredFields();
        final Function1<MutableFieldDeclaration, Boolean> _function = new Function1<MutableFieldDeclaration, Boolean>() {
          public Boolean apply(final MutableFieldDeclaration it) {
            boolean _and = false;
            boolean _and_1 = false;
            boolean _and_2 = false;
            boolean _isStatic = it.isStatic();
            boolean _not = (!_isStatic);
            if (!_not) {
              _and_2 = false;
            } else {
              boolean _isVolatile = it.isVolatile();
              boolean _not_1 = (!_isVolatile);
              _and_2 = _not_1;
            }
            if (!_and_2) {
              _and_1 = false;
            } else {
              String _simpleName = it.getSimpleName();
              boolean _startsWith = _simpleName.startsWith("_");
              boolean _not_2 = (!_startsWith);
              _and_1 = _not_2;
            }
            if (!_and_1) {
              _and = false;
            } else {
              Visibility _visibility = it.getVisibility();
              boolean _in = IterableExtensions.<Visibility>in(_visibility, Visibility.PROTECTED, Visibility.DEFAULT);
              boolean _not_3 = (!_in);
              _and = _not_3;
            }
            return Boolean.valueOf(_and);
          }
        };
        final Iterable<? extends MutableFieldDeclaration> getSetFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(_declaredFields, _function);
        final Function1<MutableFieldDeclaration, Boolean> _function_1 = new Function1<MutableFieldDeclaration, Boolean>() {
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _newTypeReference = context.newTypeReference(Ignore.class);
            Type _type = _newTypeReference.getType();
            AnnotationReference _findAnnotation = it.findAnnotation(_type);
            return Boolean.valueOf(Objects.equal(_findAnnotation, null));
          }
        };
        final Iterable<? extends MutableFieldDeclaration> reactiveFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(getSetFields, _function_1);
        final Function1<MutableFieldDeclaration, Boolean> _function_2 = new Function1<MutableFieldDeclaration, Boolean>() {
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _newTypeReference = context.newTypeReference(Require.class);
            Type _type = _newTypeReference.getType();
            AnnotationReference _findAnnotation = it.findAnnotation(_type);
            return Boolean.valueOf((!Objects.equal(_findAnnotation, null)));
          }
        };
        final Iterable<? extends MutableFieldDeclaration> requiredFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(getSetFields, _function_2);
        StringConcatenation _builder = new StringConcatenation();
        String _docComment = cls.getDocComment();
        _builder.append(_docComment, "");
        _builder.newLineIfNotEmpty();
        _builder.append("<p>");
        _builder.newLine();
        _builder.append("Part of this class source code is autogenerated by the @Reactive active annotation.");
        _builder.newLine();
        _builder.append("Please see @Reactive and ReactiveObject for more information on ReactiveObjects.");
        _builder.newLine();
        cls.setDocComment(_builder.toString());
        for (final MutableFieldDeclaration field : reactiveFields) {
          String _stopObservingFunctionName = this.getStopObservingFunctionName(field);
          final Procedure1<MutableFieldDeclaration> _function_3 = new Procedure1<MutableFieldDeclaration>() {
            public void apply(final MutableFieldDeclaration it) {
              it.setType(stopObservingType);
              it.setVisibility(Visibility.PROTECTED);
              it.setTransient(true);
            }
          };
          cls.addField(_stopObservingFunctionName, _function_3);
        }
        final Procedure1<MutableConstructorDeclaration> _function_4 = new Procedure1<MutableConstructorDeclaration>() {
          public void apply(final MutableConstructorDeclaration it) {
            EntityProcessor.this.addClassTypeParameters(it, cls, context);
            final CompilationStrategy _function = new CompilationStrategy() {
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("this.setPublishing(false);");
                _builder.newLine();
                String _classTypeParameterAssignmentCode = EntityProcessor.this.getClassTypeParameterAssignmentCode(cls);
                _builder.append(_classTypeParameterAssignmentCode, "");
                _builder.newLineIfNotEmpty();
                _builder.append("this.setPublishing(true);");
                _builder.newLine();
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addConstructor(_function_4);
        int _length = ((Object[])Conversions.unwrapArray(requiredFields, Object.class)).length;
        boolean _greaterThan = (_length > 0);
        if (_greaterThan) {
          final Procedure1<MutableConstructorDeclaration> _function_5 = new Procedure1<MutableConstructorDeclaration>() {
            public void apply(final MutableConstructorDeclaration it) {
              EntityProcessor.this.addClassTypeParameters(it, cls, context);
              for (final MutableFieldDeclaration field : requiredFields) {
                String _simpleName = field.getSimpleName();
                TypeReference _type = field.getType();
                it.addParameter(_simpleName, _type);
              }
              final CompilationStrategy _function = new CompilationStrategy() {
                public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                  StringConcatenation _builder = new StringConcatenation();
                  _builder.append("this.setPublishing(false);");
                  _builder.newLine();
                  String _classTypeParameterAssignmentCode = EntityProcessor.this.getClassTypeParameterAssignmentCode(cls);
                  _builder.append(_classTypeParameterAssignmentCode, "");
                  _builder.newLineIfNotEmpty();
                  {
                    for(final MutableFieldDeclaration field : requiredFields) {
                      _builder.append("this.set");
                      String _simpleName = field.getSimpleName();
                      String _firstUpper = StringExtensions.toFirstUpper(_simpleName);
                      _builder.append(_firstUpper, "");
                      _builder.append("(");
                      String _simpleName_1 = field.getSimpleName();
                      _builder.append(_simpleName_1, "");
                      _builder.append(");");
                      _builder.newLineIfNotEmpty();
                    }
                  }
                  _builder.append("this.setPublishing(true);");
                  _builder.newLine();
                  return _builder;
                }
              };
              it.setBody(_function);
            }
          };
          cls.addConstructor(_function_5);
        }
        int _length_1 = ((Object[])Conversions.unwrapArray(reactiveFields, Object.class)).length;
        int _length_2 = ((Object[])Conversions.unwrapArray(requiredFields, Object.class)).length;
        boolean _greaterThan_1 = (_length_1 > _length_2);
        if (_greaterThan_1) {
          final Procedure1<MutableConstructorDeclaration> _function_6 = new Procedure1<MutableConstructorDeclaration>() {
            public void apply(final MutableConstructorDeclaration it) {
              EntityProcessor.this.addClassTypeParameters(it, cls, context);
              for (final MutableFieldDeclaration field : reactiveFields) {
                String _simpleName = field.getSimpleName();
                TypeReference _type = field.getType();
                it.addParameter(_simpleName, _type);
              }
              final CompilationStrategy _function = new CompilationStrategy() {
                public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                  StringConcatenation _builder = new StringConcatenation();
                  _builder.append("this.setPublishing(false);");
                  _builder.newLine();
                  String _classTypeParameterAssignmentCode = EntityProcessor.this.getClassTypeParameterAssignmentCode(cls);
                  _builder.append(_classTypeParameterAssignmentCode, "");
                  _builder.newLineIfNotEmpty();
                  {
                    for(final MutableFieldDeclaration field : reactiveFields) {
                      _builder.append("this.set");
                      String _simpleName = field.getSimpleName();
                      String _firstUpper = StringExtensions.toFirstUpper(_simpleName);
                      _builder.append(_firstUpper, "");
                      _builder.append("(");
                      String _simpleName_1 = field.getSimpleName();
                      _builder.append(_simpleName_1, "");
                      _builder.append(");");
                      _builder.newLineIfNotEmpty();
                    }
                  }
                  _builder.append("this.setPublishing(true);");
                  _builder.newLine();
                  return _builder;
                }
              };
              it.setBody(_function);
            }
          };
          cls.addConstructor(_function_6);
        }
        for (final MutableFieldDeclaration field_1 : reactiveFields) {
          {
            TypeReference _type = field_1.getType();
            String _simpleName = _type.getSimpleName();
            boolean _startsWith = _simpleName.startsWith("List");
            if (_startsWith) {
              TypeReference _type_1 = field_1.getType();
              List<TypeReference> _actualTypeArguments = _type_1.getActualTypeArguments();
              boolean _isEmpty = _actualTypeArguments.isEmpty();
              if (_isEmpty) {
                context.addError(field_1, "Reactive classes may not have untyped Lists");
              }
            }
            TypeReference _type_2 = field_1.getType();
            String _simpleName_1 = _type_2.getSimpleName();
            boolean _startsWith_1 = _simpleName_1.startsWith("Map");
            if (_startsWith_1) {
              TypeReference _type_3 = field_1.getType();
              List<TypeReference> _actualTypeArguments_1 = _type_3.getActualTypeArguments();
              boolean _isEmpty_1 = _actualTypeArguments_1.isEmpty();
              if (_isEmpty_1) {
                context.addError(field_1, "Reactive classes may not have untyped Maps");
              }
            }
          }
        }
        final Procedure1<MutableMethodDeclaration> _function_7 = new Procedure1<MutableMethodDeclaration>() {
          public void apply(final MutableMethodDeclaration it) {
            TypeReference _newTypeReference = context.newTypeReference("boolean");
            it.setReturnType(_newTypeReference);
            final CompilationStrategy _function = new CompilationStrategy() {
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                {
                  for(final MutableFieldDeclaration field : requiredFields) {
                    _builder.append("if(");
                    String _simpleName = field.getSimpleName();
                    _builder.append(_simpleName, "");
                    _builder.append("==null) return false;");
                    _builder.newLineIfNotEmpty();
                  }
                }
                _builder.append("return true;");
                _builder.newLine();
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addMethod("isValid", _function_7);
        for (final MutableFieldDeclaration f : getSetFields) {
          {
            String _simpleName = f.getSimpleName();
            String _firstUpper = StringExtensions.toFirstUpper(_simpleName);
            String _plus = ("get" + _firstUpper);
            final Procedure1<MutableMethodDeclaration> _function_8 = new Procedure1<MutableMethodDeclaration>() {
              public void apply(final MutableMethodDeclaration it) {
                TypeReference _type = f.getType();
                String _simpleName = _type.getSimpleName();
                Class<? extends Object> _get = typeConversions.get(_simpleName);
                TypeReference _newTypeReference = null;
                if (_get!=null) {
                  _newTypeReference=context.newTypeReference(_get);
                }
                TypeReference _type_1 = f.getType();
                TypeReference _or = OptExtensions.<TypeReference>or(_newTypeReference, _type_1);
                it.setReturnType(_or);
                final CompilationStrategy _function = new CompilationStrategy() {
                  public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                    StringConcatenation _builder = new StringConcatenation();
                    _builder.append("return ");
                    String _simpleName = f.getSimpleName();
                    _builder.append(_simpleName, "");
                    _builder.append(";");
                    return _builder;
                  }
                };
                it.setBody(_function);
              }
            };
            cls.addMethod(_plus, _function_8);
            String _simpleName_1 = f.getSimpleName();
            String _firstUpper_1 = StringExtensions.toFirstUpper(_simpleName_1);
            String _plus_1 = ("set" + _firstUpper_1);
            final Procedure1<MutableMethodDeclaration> _function_9 = new Procedure1<MutableMethodDeclaration>() {
              public void apply(final MutableMethodDeclaration it) {
                TypeReference _type = f.getType();
                String _simpleName = _type.getSimpleName();
                Class<? extends Object> _get = typeConversions.get(_simpleName);
                TypeReference _newTypeReference = null;
                if (_get!=null) {
                  _newTypeReference=context.newTypeReference(_get);
                }
                TypeReference _type_1 = f.getType();
                final TypeReference setterType = OptExtensions.<TypeReference>or(_newTypeReference, _type_1);
                it.addParameter("value", setterType);
                final CompilationStrategy _function = new CompilationStrategy() {
                  public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                    StringConcatenation _builder = new StringConcatenation();
                    _builder.append("// stop listening to old value");
                    _builder.newLine();
                    _builder.append("if(this.");
                    String _simpleName = f.getSimpleName();
                    _builder.append(_simpleName, "");
                    _builder.append(" != null && this.");
                    String _stopObservingFunctionName = EntityProcessor.this.getStopObservingFunctionName(f);
                    _builder.append(_stopObservingFunctionName, "");
                    _builder.append(" != null)");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t");
                    String _stopObservingFunctionName_1 = EntityProcessor.this.getStopObservingFunctionName(f);
                    _builder.append(_stopObservingFunctionName_1, "\t");
                    _builder.append(".apply();");
                    _builder.newLineIfNotEmpty();
                    _builder.append("// set the value");
                    _builder.newLine();
                    _builder.append("this.");
                    String _simpleName_1 = f.getSimpleName();
                    _builder.append(_simpleName_1, "");
                    _builder.append(" = value;");
                    _builder.newLineIfNotEmpty();
                    _builder.append("// and start observing the new value for changes");
                    _builder.newLine();
                    CharSequence _observeField = EntityProcessor.this.observeField(f, context);
                    _builder.append(_observeField, "");
                    _builder.newLineIfNotEmpty();
                    _builder.append("// if we are publishing, publish the change we\'ve made");
                    _builder.newLine();
                    _builder.append("if(this.isPublishing()) {");
                    _builder.newLine();
                    {
                      boolean _or = false;
                      TypeReference _type = f.getType();
                      boolean _isPrimitive = _type.isPrimitive();
                      if (_isPrimitive) {
                        _or = true;
                      } else {
                        TypeReference _type_1 = f.getType();
                        TypeReference _newTypeReference = context.newTypeReference(String.class);
                        boolean _isAssignableFrom = _type_1.isAssignableFrom(_newTypeReference);
                        _or = _isAssignableFrom;
                      }
                      if (_or) {
                        _builder.append("\t");
                        _builder.append("getPublisher().apply(new Change(nl.kii.reactive.ChangeType.UPDATE, \"");
                        String _simpleName_2 = f.getSimpleName();
                        _builder.append(_simpleName_2, "\t");
                        _builder.append("\", value));");
                        _builder.newLineIfNotEmpty();
                      } else {
                        TypeReference _type_2 = f.getType();
                        TypeReference _newTypeReference_1 = context.newTypeReference(Map.class);
                        boolean _isAssignableFrom_1 = _type_2.isAssignableFrom(_newTypeReference_1);
                        if (_isAssignableFrom_1) {
                          _builder.append("\t");
                          _builder.append("getPublisher().apply(new Change(nl.kii.reactive.ChangeType.UPDATE, \"");
                          String _simpleName_3 = f.getSimpleName();
                          _builder.append(_simpleName_3, "\t");
                          _builder.append("\", ((");
                          TypeReference _entityMapType = EntityProcessor.this.toEntityMapType(f, context);
                          String _name = _entityMapType.getName();
                          _builder.append(_name, "\t");
                          _builder.append(")this.");
                          String _simpleName_4 = f.getSimpleName();
                          _builder.append(_simpleName_4, "\t");
                          _builder.append(").clone()));");
                          _builder.newLineIfNotEmpty();
                        } else {
                          TypeReference _type_3 = f.getType();
                          TypeReference _newTypeReference_2 = context.newTypeReference(List.class);
                          boolean _isAssignableFrom_2 = _type_3.isAssignableFrom(_newTypeReference_2);
                          if (_isAssignableFrom_2) {
                            _builder.append("\t");
                            _builder.append("getPublisher().apply(new Change(nl.kii.reactive.ChangeType.UPDATE, \"");
                            String _simpleName_5 = f.getSimpleName();
                            _builder.append(_simpleName_5, "\t");
                            _builder.append("\", ((");
                            TypeReference _entityListType = EntityProcessor.this.toEntityListType(f, context);
                            String _name_1 = _entityListType.getName();
                            _builder.append(_name_1, "\t");
                            _builder.append(")this.");
                            String _simpleName_6 = f.getSimpleName();
                            _builder.append(_simpleName_6, "\t");
                            _builder.append(").clone()));");
                            _builder.newLineIfNotEmpty();
                          } else {
                            _builder.append("\t");
                            _builder.append("getPublisher().apply(new Change(nl.kii.reactive.ChangeType.UPDATE, \"");
                            String _simpleName_7 = f.getSimpleName();
                            _builder.append(_simpleName_7, "\t");
                            _builder.append("\", this.");
                            String _simpleName_8 = f.getSimpleName();
                            _builder.append(_simpleName_8, "\t");
                            _builder.append(".clone()));");
                            _builder.newLineIfNotEmpty();
                          }
                        }
                      }
                    }
                    _builder.append("}");
                    _builder.newLine();
                    return _builder;
                  }
                };
                it.setBody(_function);
              }
            };
            cls.addMethod(_plus_1, _function_9);
          }
        }
        final Procedure1<MutableMethodDeclaration> _function_8 = new Procedure1<MutableMethodDeclaration>() {
          public void apply(final MutableMethodDeclaration it) {
            it.setSynchronized(true);
            TypeReference _newTypeReference = context.newTypeReference(Change.class);
            it.addParameter("change", _newTypeReference);
            final CompilationStrategy _function = new CompilationStrategy() {
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("boolean wasPublishing = this.isPublishing();");
                _builder.newLine();
                _builder.append("try {");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("// do not publish when applying, to prevent update loops");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("this.setPublishing(false);");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("// determine what to change using the change path");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("if(change.getPath() == null || change.getPath().size() == 0) {");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("// change applies to this object, check the type");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("if(change.getValue() == null)");
                _builder.newLine();
                _builder.append("\t\t\t");
                _builder.append("throw new NullPointerException(\"incoming change has no value: \" + change);");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("if(!(change.getValue() instanceof ");
                String _name = clsType.getName();
                _builder.append(_name, "\t\t");
                _builder.append(")) ");
                _builder.newLineIfNotEmpty();
                _builder.append("\t\t\t");
                _builder.append("throw new IllegalArgumentException(\"incoming change has a value of the wrong type: \" + change + \", expected \" + this.getClass().getName());");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("// assign the all fields directly from the value of the change");
                _builder.newLine();
                _builder.append("\t\t");
                String _name_1 = clsType.getName();
                _builder.append(_name_1, "\t\t");
                _builder.append(" value = ((");
                String _name_2 = clsType.getName();
                _builder.append(_name_2, "\t\t");
                _builder.append(")change.getValue()).clone();");
                _builder.newLineIfNotEmpty();
                {
                  for(final MutableFieldDeclaration field : reactiveFields) {
                    _builder.append("\t\t");
                    _builder.append("if(value.");
                    String _simpleName = field.getSimpleName();
                    _builder.append(_simpleName, "\t\t");
                    _builder.append(" != null) ");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t");
                    _builder.append("this.set");
                    String _simpleName_1 = field.getSimpleName();
                    String _firstUpper = StringExtensions.toFirstUpper(_simpleName_1);
                    _builder.append(_firstUpper, "\t\t\t");
                    _builder.append("(value.");
                    String _simpleName_2 = field.getSimpleName();
                    _builder.append(_simpleName_2, "\t\t\t");
                    _builder.append(");");
                    _builder.newLineIfNotEmpty();
                  }
                }
                _builder.append("\t");
                _builder.append("} else if(change.getPath().size() == 1) {");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("// change applies directly to a field of this object");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("String field = change.getPath().get(0);");
                _builder.newLine();
                {
                  for(final MutableFieldDeclaration field_1 : reactiveFields) {
                    _builder.append("\t\t");
                    _builder.append("if(field.equals(\"");
                    String _simpleName_3 = field_1.getSimpleName();
                    _builder.append(_simpleName_3, "\t\t");
                    _builder.append("\")) {");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t");
                    _builder.append("switch(change.getAction()) {");
                    _builder.newLine();
                    _builder.append("\t\t");
                    _builder.append("\t\t");
                    _builder.append("case UPDATE:");
                    _builder.newLine();
                    _builder.append("\t\t");
                    _builder.append("\t\t\t");
                    _builder.append("this.set");
                    String _simpleName_4 = field_1.getSimpleName();
                    String _firstUpper_1 = StringExtensions.toFirstUpper(_simpleName_4);
                    _builder.append(_firstUpper_1, "\t\t\t\t\t");
                    _builder.append("((");
                    TypeReference _type = field_1.getType();
                    String _simpleName_5 = _type.getSimpleName();
                    _builder.append(_simpleName_5, "\t\t\t\t\t");
                    _builder.append(")change.getValue());");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t\t\t");
                    _builder.append("// this.");
                    String _simpleName_6 = field_1.getSimpleName();
                    _builder.append(_simpleName_6, "\t\t\t\t\t");
                    _builder.append(" = (");
                    TypeReference _type_1 = field_1.getType();
                    String _simpleName_7 = _type_1.getSimpleName();
                    _builder.append(_simpleName_7, "\t\t\t\t\t");
                    _builder.append(")change.getValue();");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t\t\t");
                    _builder.append("break;");
                    _builder.newLine();
                    _builder.append("\t\t");
                    _builder.append("\t\t");
                    _builder.append("case CLEAR:");
                    _builder.newLine();
                    _builder.append("\t\t");
                    _builder.append("\t\t\t");
                    _builder.append("this.");
                    String _simpleName_8 = field_1.getSimpleName();
                    _builder.append(_simpleName_8, "\t\t\t\t\t");
                    _builder.append(" = null;");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t\t\t");
                    _builder.append("break;");
                    _builder.newLine();
                    _builder.append("\t\t");
                    _builder.append("\t\t");
                    _builder.append("default: throw new IllegalArgumentException(\"cannot update field ");
                    String _simpleName_9 = field_1.getSimpleName();
                    _builder.append(_simpleName_9, "\t\t\t\t");
                    _builder.append(" of entity ");
                    String _simpleName_10 = clsType.getSimpleName();
                    _builder.append(_simpleName_10, "\t\t\t\t");
                    _builder.append(" with \" + change + \", must be an UPDATE or CLEAR command\");");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t");
                    _builder.append("}");
                    _builder.newLine();
                    _builder.append("\t\t");
                    _builder.append("}");
                    _builder.newLine();
                  }
                }
                _builder.append("\t");
                _builder.append("} else {");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("// change goes deeper inside of of the fields, propagate the path inside that field");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("String field = change.getPath().get(0);");
                _builder.newLine();
                {
                  for(final MutableFieldDeclaration field_2 : reactiveFields) {
                    _builder.append("\t\t");
                    _builder.append("if(field.equals(\"");
                    String _simpleName_11 = field_2.getSimpleName();
                    _builder.append(_simpleName_11, "\t\t");
                    _builder.append("\")) {");
                    _builder.newLineIfNotEmpty();
                    {
                      TypeReference _type_2 = field_2.getType();
                      boolean _isAssignableFrom = reactiveType.isAssignableFrom(_type_2);
                      if (_isAssignableFrom) {
                        _builder.append("\t\t");
                        _builder.append("\t");
                        String _simpleName_12 = field_2.getSimpleName();
                        _builder.append(_simpleName_12, "\t\t\t");
                        _builder.append(".apply(change.forward());");
                        _builder.newLineIfNotEmpty();
                      } else {
                        _builder.append("\t\t");
                        _builder.append("\t");
                        _builder.append("throw new IllegalArgumentException(\"cannot update field ");
                        String _simpleName_13 = field_2.getSimpleName();
                        _builder.append(_simpleName_13, "\t\t\t");
                        _builder.append(" of entity ");
                        String _simpleName_14 = clsType.getSimpleName();
                        _builder.append(_simpleName_14, "\t\t\t");
                        _builder.append(" with \" + change + \", the field is not Reactive\");");
                        _builder.newLineIfNotEmpty();
                      }
                    }
                    _builder.append("\t\t");
                    _builder.append("}");
                    _builder.newLine();
                  }
                }
                _builder.append("\t");
                _builder.append("}");
                _builder.newLine();
                _builder.append("} finally {");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("this.setPublishing(wasPublishing);");
                _builder.newLine();
                _builder.append("}");
                _builder.newLine();
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addMethod("apply", _function_8);
        final Procedure1<MutableMethodDeclaration> _function_9 = new Procedure1<MutableMethodDeclaration>() {
          public void apply(final MutableMethodDeclaration it) {
            final TypeReference stringType = context.newTypeReference(String.class);
            TypeReference _string = context.getString();
            it.setReturnType(_string);
            final CompilationStrategy _function = new CompilationStrategy() {
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("return \"");
                String _simpleName = cls.getSimpleName();
                _builder.append(_simpleName, "");
                _builder.append(" { \"");
                _builder.newLineIfNotEmpty();
                {
                  boolean _hasElements = false;
                  for(final MutableFieldDeclaration field : reactiveFields) {
                    if (!_hasElements) {
                      _hasElements = true;
                    } else {
                      _builder.appendImmediate(" + \", \" ", "");
                    }
                    _builder.append("+ \"");
                    String _simpleName_1 = field.getSimpleName();
                    _builder.append(_simpleName_1, "");
                    _builder.append(": \" +");
                    _builder.newLineIfNotEmpty();
                    {
                      TypeReference _type = field.getType();
                      boolean _isAssignableFrom = _type.isAssignableFrom(stringType);
                      if (_isAssignableFrom) {
                        _builder.append("\"\'\" + this.");
                        String _simpleName_2 = field.getSimpleName();
                        _builder.append(_simpleName_2, "");
                        _builder.append(" + \"\'\" ");
                        _builder.newLineIfNotEmpty();
                      } else {
                        _builder.append("this.");
                        String _simpleName_3 = field.getSimpleName();
                        _builder.append(_simpleName_3, "");
                        _builder.append(" ");
                        _builder.newLineIfNotEmpty();
                      }
                    }
                  }
                }
                _builder.append("+ \" }\";");
                _builder.newLine();
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addMethod("toString", _function_9);
        Iterable<? extends MutableMethodDeclaration> _declaredMethods = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_10 = new Function1<MutableMethodDeclaration, Boolean>() {
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "equals"));
          }
        };
        MutableMethodDeclaration _findFirst = org.eclipse.xtext.xbase.lib.IterableExtensions.findFirst(_declaredMethods, _function_10);
        boolean _defined = OptExtensions.<Object>defined(_findFirst);
        boolean _not = (!_defined);
        if (_not) {
          final Procedure1<MutableMethodDeclaration> _function_11 = new Procedure1<MutableMethodDeclaration>() {
            public void apply(final MutableMethodDeclaration it) {
              TypeReference _object = context.getObject();
              it.addParameter("object", _object);
              TypeReference _primitiveBoolean = context.getPrimitiveBoolean();
              it.setReturnType(_primitiveBoolean);
              final CompilationStrategy _function = new CompilationStrategy() {
                public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                  StringConcatenation _builder = new StringConcatenation();
                  _builder.append("if(object instanceof ");
                  String _simpleName = cls.getSimpleName();
                  _builder.append(_simpleName, "");
                  _builder.append(") {");
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t");
                  _builder.append("return (");
                  _builder.newLine();
                  {
                    boolean _hasElements = false;
                    for(final MutableFieldDeclaration field : getSetFields) {
                      if (!_hasElements) {
                        _hasElements = true;
                      } else {
                        _builder.appendImmediate(" && ", "\t\t");
                      }
                      _builder.append("\t\t");
                      _builder.append("(");
                      _builder.newLine();
                      _builder.append("\t\t");
                      _builder.append("\t");
                      _builder.append("(this.");
                      String _simpleName_1 = field.getSimpleName();
                      _builder.append(_simpleName_1, "\t\t\t");
                      _builder.append(" == null && ((");
                      String _simpleName_2 = cls.getSimpleName();
                      _builder.append(_simpleName_2, "\t\t\t");
                      _builder.append(") object).");
                      String _simpleName_3 = field.getSimpleName();
                      _builder.append(_simpleName_3, "\t\t\t");
                      _builder.append(" == null) ||");
                      _builder.newLineIfNotEmpty();
                      _builder.append("\t\t");
                      _builder.append("\t");
                      _builder.append("(");
                      _builder.newLine();
                      _builder.append("\t\t");
                      _builder.append("\t\t");
                      _builder.append("this.");
                      String _simpleName_4 = field.getSimpleName();
                      _builder.append(_simpleName_4, "\t\t\t\t");
                      _builder.append(" != null && ");
                      _builder.newLineIfNotEmpty();
                      _builder.append("\t\t");
                      _builder.append("\t\t");
                      _builder.append("this.");
                      String _simpleName_5 = field.getSimpleName();
                      _builder.append(_simpleName_5, "\t\t\t\t");
                      _builder.append(".equals(((");
                      String _simpleName_6 = cls.getSimpleName();
                      _builder.append(_simpleName_6, "\t\t\t\t");
                      _builder.append(") object).");
                      String _simpleName_7 = field.getSimpleName();
                      _builder.append(_simpleName_7, "\t\t\t\t");
                      _builder.append(")");
                      _builder.newLineIfNotEmpty();
                      _builder.append("\t\t");
                      _builder.append("\t");
                      _builder.append(") ");
                      _builder.newLine();
                      _builder.append("\t\t");
                      _builder.append(")");
                      _builder.newLine();
                    }
                  }
                  _builder.append("\t");
                  _builder.append(");");
                  _builder.newLine();
                  _builder.append("} else return false;");
                  _builder.newLine();
                  return _builder;
                }
              };
              it.setBody(_function);
            }
          };
          cls.addMethod("equals", _function_11);
        }
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_1 = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_12 = new Function1<MutableMethodDeclaration, Boolean>() {
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "hashCode"));
          }
        };
        MutableMethodDeclaration _findFirst_1 = org.eclipse.xtext.xbase.lib.IterableExtensions.findFirst(_declaredMethods_1, _function_12);
        boolean _defined_1 = OptExtensions.<Object>defined(_findFirst_1);
        boolean _not_1 = (!_defined_1);
        if (_not_1) {
          final Procedure1<MutableMethodDeclaration> _function_13 = new Procedure1<MutableMethodDeclaration>() {
            public void apply(final MutableMethodDeclaration it) {
              TypeReference _primitiveInt = context.getPrimitiveInt();
              it.setReturnType(_primitiveInt);
              final CompilationStrategy _function = new CompilationStrategy() {
                public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                  StringConcatenation _builder = new StringConcatenation();
                  _builder.append("return (");
                  _builder.newLine();
                  {
                    boolean _hasElements = false;
                    for(final MutableFieldDeclaration field : getSetFields) {
                      if (!_hasElements) {
                        _hasElements = true;
                      } else {
                        _builder.appendImmediate(" + ", "\t");
                      }
                      _builder.append("\t");
                      _builder.append("((this.");
                      String _simpleName = field.getSimpleName();
                      _builder.append(_simpleName, "\t");
                      _builder.append(" != null) ?");
                      _builder.newLineIfNotEmpty();
                      {
                        TypeReference _type = field.getType();
                        boolean _isPrimitive = _type.isPrimitive();
                        if (_isPrimitive) {
                          _builder.append("\t");
                          _builder.append("(this.");
                          String _simpleName_1 = field.getSimpleName();
                          _builder.append(_simpleName_1, "\t");
                          _builder.append(" + \"\").hashCode()");
                          _builder.newLineIfNotEmpty();
                        } else {
                          _builder.append("\t");
                          _builder.append("this.");
                          String _simpleName_2 = field.getSimpleName();
                          _builder.append(_simpleName_2, "\t");
                          _builder.append(".hashCode()");
                          _builder.newLineIfNotEmpty();
                        }
                      }
                      _builder.append("\t");
                      _builder.append(": 0)");
                      _builder.newLine();
                    }
                  }
                  _builder.append(") * 37;");
                  _builder.newLine();
                  return _builder;
                }
              };
              it.setBody(_function);
            }
          };
          cls.addMethod("hashCode", _function_13);
        }
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_2 = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_14 = new Function1<MutableMethodDeclaration, Boolean>() {
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "clone"));
          }
        };
        MutableMethodDeclaration _findFirst_2 = org.eclipse.xtext.xbase.lib.IterableExtensions.findFirst(_declaredMethods_2, _function_14);
        boolean _defined_2 = OptExtensions.<Object>defined(_findFirst_2);
        boolean _not_2 = (!_defined_2);
        if (_not_2) {
          final Procedure1<MutableMethodDeclaration> _function_15 = new Procedure1<MutableMethodDeclaration>() {
            public void apply(final MutableMethodDeclaration it) {
              it.setReturnType(clsType);
              final CompilationStrategy _function = new CompilationStrategy() {
                public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                  StringConcatenation _builder = new StringConcatenation();
                  _builder.append("try {");
                  _builder.newLine();
                  _builder.append("\t");
                  _builder.append("return (");
                  String _simpleName = clsType.getSimpleName();
                  _builder.append(_simpleName, "\t");
                  _builder.append(")super.clone();");
                  _builder.newLineIfNotEmpty();
                  _builder.append("} catch(CloneNotSupportedException e) {");
                  _builder.newLine();
                  _builder.append("\t");
                  _builder.append("return null;");
                  _builder.newLine();
                  _builder.append("}");
                  _builder.newLine();
                  return _builder;
                }
              };
              it.setBody(_function);
            }
          };
          cls.addMethod("clone", _function_15);
        }
        final Procedure1<MutableMethodDeclaration> _function_16 = new Procedure1<MutableMethodDeclaration>() {
          public void apply(final MutableMethodDeclaration it) {
            it.setVisibility(Visibility.PROTECTED);
            it.setDocComment("creates a listener for propagating to changes on a field to the publisher");
            TypeReference _string = context.getString();
            it.addParameter("path", _string);
            it.setReturnType(changeHandlerType);
            final CompilationStrategy _function = new CompilationStrategy() {
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("final ");
                String _simpleName = clsType.getSimpleName();
                _builder.append(_simpleName, "");
                _builder.append(" entity = this;");
                _builder.newLineIfNotEmpty();
                _builder.append("return new Procedure1<Change>() {");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("public void apply(Change change) {");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("//only propagate a change if we can publish");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("if(hasPublisher())");
                _builder.newLine();
                _builder.append("\t\t\t");
                _builder.append("getPublisher().apply(change.addPath(path));");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("}");
                _builder.newLine();
                _builder.append("};");
                _builder.newLine();
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addMethod("newChangeHandler", _function_16);
      }
    }
  }
  
  public CharSequence observeField(final MutableFieldDeclaration f, @Extension final TransformationContext context) {
    StringConcatenation _builder = new StringConcatenation();
    {
      TypeReference _type = f.getType();
      String _simpleName = _type.getSimpleName();
      boolean _startsWith = _simpleName.startsWith("List");
      if (_startsWith) {
        _builder.append("// if the list is not already reactive, wrap the list as a reactive list");
        _builder.newLine();
        _builder.append("if(");
        String _simpleName_1 = f.getSimpleName();
        _builder.append(_simpleName_1, "");
        _builder.append(" == null || !(");
        String _simpleName_2 = f.getSimpleName();
        _builder.append(_simpleName_2, "");
        _builder.append(" instanceof  nl.kii.reactive.EntityList<?>)) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        TypeReference _type_1 = f.getType();
        List<TypeReference> _actualTypeArguments = _type_1.getActualTypeArguments();
        final TypeReference typeArg = _actualTypeArguments.get(0);
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        final TypeReference listType = context.newTypeReference(EntityList.class, typeArg);
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        String _name = listType.getName();
        _builder.append(_name, "\t");
        _builder.append(" newList = new ");
        String _name_1 = listType.getName();
        _builder.append(_name_1, "\t");
        _builder.append("(");
        String _name_2 = typeArg.getName();
        _builder.append(_name_2, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("if(");
        String _simpleName_3 = f.getSimpleName();
        _builder.append(_simpleName_3, "\t");
        _builder.append(" != null) newList.addAll(");
        String _simpleName_4 = f.getSimpleName();
        _builder.append(_simpleName_4, "\t");
        _builder.append(");");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        String _simpleName_5 = f.getSimpleName();
        _builder.append(_simpleName_5, "\t");
        _builder.append(" = newList;");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("this.");
        String _stopObservingFunctionName = this.getStopObservingFunctionName(f);
        _builder.append(_stopObservingFunctionName, "\t");
        _builder.append(" = newList.onChange(newChangeHandler(\"");
        String _simpleName_6 = f.getSimpleName();
        _builder.append(_simpleName_6, "\t");
        _builder.append("\"));");
        _builder.newLineIfNotEmpty();
        _builder.append("}");
        _builder.newLine();
      } else {
        TypeReference _type_2 = f.getType();
        String _simpleName_7 = _type_2.getSimpleName();
        boolean _startsWith_1 = _simpleName_7.startsWith("Map");
        if (_startsWith_1) {
          _builder.append("// if the map is not already listenable, wrap the map as a listenable");
          _builder.newLine();
          _builder.append("if(");
          String _simpleName_8 = f.getSimpleName();
          _builder.append(_simpleName_8, "");
          _builder.append(" == null || !(");
          String _simpleName_9 = f.getSimpleName();
          _builder.append(_simpleName_9, "");
          _builder.append(" instanceof  nl.kii.reactive.EntityMap<?>)) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          TypeReference _type_3 = f.getType();
          List<TypeReference> _actualTypeArguments_1 = _type_3.getActualTypeArguments();
          final TypeReference typeArg_1 = _actualTypeArguments_1.get(1);
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          final TypeReference mapType = context.newTypeReference(EntityMap.class, typeArg_1);
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          String _name_3 = mapType.getName();
          _builder.append(_name_3, "\t");
          _builder.append(" newMap = new ");
          String _name_4 = mapType.getName();
          _builder.append(_name_4, "\t");
          _builder.append("(");
          String _simpleName_10 = typeArg_1.getSimpleName();
          _builder.append(_simpleName_10, "\t");
          _builder.append(".class);");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("if(");
          String _simpleName_11 = f.getSimpleName();
          _builder.append(_simpleName_11, "\t");
          _builder.append(" != null) newMap.putAll(");
          String _simpleName_12 = f.getSimpleName();
          _builder.append(_simpleName_12, "\t");
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          String _simpleName_13 = f.getSimpleName();
          _builder.append(_simpleName_13, "\t");
          _builder.append(" = newMap;");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("this.");
          String _stopObservingFunctionName_1 = this.getStopObservingFunctionName(f);
          _builder.append(_stopObservingFunctionName_1, "\t");
          _builder.append(" = newMap.onChange(newChangeHandler(\"");
          String _simpleName_14 = f.getSimpleName();
          _builder.append(_simpleName_14, "\t");
          _builder.append("\"));");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        } else {
          TypeReference _newTypeReference = context.newTypeReference(Change.class);
          TypeReference _newTypeReference_1 = context.newTypeReference(Observable.class, _newTypeReference);
          TypeReference _type_4 = f.getType();
          boolean _isAssignableFrom = _newTypeReference_1.isAssignableFrom(_type_4);
          if (_isAssignableFrom) {
            _builder.append("this.");
            String _stopObservingFunctionName_2 = this.getStopObservingFunctionName(f);
            _builder.append(_stopObservingFunctionName_2, "");
            _builder.append(" = this.");
            String _simpleName_15 = f.getSimpleName();
            _builder.append(_simpleName_15, "");
            _builder.append(".onChange(newChangeHandler(\"");
            String _simpleName_16 = f.getSimpleName();
            _builder.append(_simpleName_16, "");
            _builder.append("\"));");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    }
    return _builder;
  }
  
  public String getStopObservingFunctionName(final MutableFieldDeclaration field) {
    String _simpleName = field.getSimpleName();
    String _firstUpper = StringExtensions.toFirstUpper(_simpleName);
    String _plus = ("stopObserving" + _firstUpper);
    return (_plus + "Fn");
  }
  
  public String getTypeParamName(final TypeParameterDeclaration type, final int position) {
    return ("typeParam" + Integer.valueOf(position));
  }
  
  public TypeReference toEntityMapType(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    TypeReference _type = field.getType();
    List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
    TypeReference _get = _actualTypeArguments.get(1);
    return context.newTypeReference(EntityMap.class, _get);
  }
  
  public TypeReference toEntityListType(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    TypeReference _type = field.getType();
    List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
    TypeReference _get = _actualTypeArguments.get(0);
    return context.newTypeReference(EntityList.class, _get);
  }
  
  public void addClassTypeParameters(final MutableExecutableDeclaration constructor, final ClassDeclaration cls, @Extension final TransformationContext context) {
    Iterable<? extends TypeParameterDeclaration> _typeParameters = cls.getTypeParameters();
    final Procedure2<TypeParameterDeclaration, Integer> _function = new Procedure2<TypeParameterDeclaration, Integer>() {
      public void apply(final TypeParameterDeclaration param, final Integer count) {
        String _typeParamName = EntityProcessor.this.getTypeParamName(param, ((count).intValue() + 1));
        TypeReference _newTypeReference = context.newTypeReference(param);
        TypeReference _newTypeReference_1 = context.newTypeReference(Class.class, _newTypeReference);
        constructor.addParameter(_typeParamName, _newTypeReference_1);
      }
    };
    org.eclipse.xtext.xbase.lib.IterableExtensions.forEach(_typeParameters, _function);
  }
  
  public String getClassTypeParameterAssignmentCode(final ClassDeclaration cls) {
    String _xblockexpression = null;
    {
      final List<String> list = CollectionLiterals.<String>newLinkedList();
      Iterable<? extends TypeParameterDeclaration> _typeParameters = cls.getTypeParameters();
      final Procedure2<TypeParameterDeclaration, Integer> _function = new Procedure2<TypeParameterDeclaration, Integer>() {
        public void apply(final TypeParameterDeclaration param, final Integer count) {
          final String name = EntityProcessor.this.getTypeParamName(param, ((count).intValue() + 1));
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("this.");
          _builder.append(name, "");
          _builder.append(" = ");
          _builder.append(name, "");
          _builder.append(";");
          IterableExtensions.<String>operator_doubleLessThan(list, _builder.toString());
        }
      };
      org.eclipse.xtext.xbase.lib.IterableExtensions.forEach(_typeParameters, _function);
      _xblockexpression = org.eclipse.xtext.xbase.lib.IterableExtensions.join(list, "\n");
    }
    return _xblockexpression;
  }
  
  public List<String> getClassTypeParameters(final ClassDeclaration cls) {
    List<String> _xblockexpression = null;
    {
      final List<String> list = CollectionLiterals.<String>newLinkedList();
      Iterable<? extends TypeParameterDeclaration> _typeParameters = cls.getTypeParameters();
      final Procedure2<TypeParameterDeclaration, Integer> _function = new Procedure2<TypeParameterDeclaration, Integer>() {
        public void apply(final TypeParameterDeclaration param, final Integer count) {
          String _typeParamName = EntityProcessor.this.getTypeParamName(param, ((count).intValue() + 1));
          IterableExtensions.<String>operator_doubleLessThan(list, _typeParamName);
        }
      };
      org.eclipse.xtext.xbase.lib.IterableExtensions.forEach(_typeParameters, _function);
      _xblockexpression = list;
    }
    return _xblockexpression;
  }
}
