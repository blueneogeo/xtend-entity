package nl.kii.entity.annotations;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.kii.entity.Change;
import nl.kii.entity.EntityList;
import nl.kii.entity.EntityMap;
import nl.kii.entity.ReactiveObject;
import nl.kii.entity.annotations.Ignore;
import nl.kii.entity.annotations.Require;
import nl.kii.observe.Observable;
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
import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration;
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
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.eclipse.xtext.xbase.lib.StringExtensions;

/**
 * Active Annotation Processor for Entity annotations.
 * @see Entity
 */
@SuppressWarnings("all")
public class EntityProcessor implements TransformationParticipant<MutableClassDeclaration> {
  public void doTransform(final List<? extends MutableClassDeclaration> classes, @Extension final TransformationContext context) {
    TypeReference _newTypeReference = context.newTypeReference(Change.class);
    final TypeReference changeHandlerType = context.newTypeReference(Procedure1.class, _newTypeReference);
    final TypeReference stopObservingType = context.newTypeReference(Procedure0.class);
    Pair<String, Class<Boolean>> _mappedTo = Pair.<String, Class<Boolean>>of("boolean", Boolean.class);
    Pair<String, Class<Integer>> _mappedTo_1 = Pair.<String, Class<Integer>>of("int", Integer.class);
    Pair<String, Class<Long>> _mappedTo_2 = Pair.<String, Class<Long>>of("long", Long.class);
    Pair<String, Class<Float>> _mappedTo_3 = Pair.<String, Class<Float>>of("float", Float.class);
    Pair<String, Class<Double>> _mappedTo_4 = Pair.<String, Class<Double>>of("double", Double.class);
    final Map<String, Class<? extends Object>> typeConversions = Collections.<String, Class<? extends Object>>unmodifiableMap(CollectionLiterals.<String, Class<? extends Object>>newHashMap(_mappedTo, _mappedTo_1, _mappedTo_2, _mappedTo_3, _mappedTo_4));
    for (final MutableClassDeclaration cls : classes) {
      {
        final TypeReference clsType = context.newTypeReference(cls);
        TypeReference _newTypeReference_1 = context.newTypeReference(ReactiveObject.class);
        cls.setExtendedClass(_newTypeReference_1);
        Iterable<? extends TypeReference> _implementedInterfaces = cls.getImplementedInterfaces();
        TypeReference _newTypeReference_2 = context.newTypeReference(Cloneable.class);
        Iterable<TypeReference> _plus = Iterables.<TypeReference>concat(_implementedInterfaces, Collections.<TypeReference>unmodifiableList(CollectionLiterals.<TypeReference>newArrayList(_newTypeReference_2)));
        cls.setImplementedInterfaces(_plus);
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
            TypeReference _newTypeReference = context.newTypeReference(Require.class);
            Type _type = _newTypeReference.getType();
            AnnotationReference _findAnnotation = it.findAnnotation(_type);
            return Boolean.valueOf((!Objects.equal(_findAnnotation, null)));
          }
        };
        final Iterable<? extends MutableFieldDeclaration> requiredFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(getSetFields, _function_1);
        final Function1<MutableFieldDeclaration, Boolean> _function_2 = new Function1<MutableFieldDeclaration, Boolean>() {
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _newTypeReference = context.newTypeReference(Ignore.class);
            Type _type = _newTypeReference.getType();
            AnnotationReference _findAnnotation = it.findAnnotation(_type);
            return Boolean.valueOf(Objects.equal(_findAnnotation, null));
          }
        };
        final Iterable<? extends MutableFieldDeclaration> observedFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(getSetFields, _function_2);
        final Function1<MutableFieldDeclaration, Boolean> _function_3 = new Function1<MutableFieldDeclaration, Boolean>() {
          public Boolean apply(final MutableFieldDeclaration it) {
            return Boolean.valueOf(EntityProcessor.this.isReactive(it, context));
          }
        };
        final Iterable<? extends MutableFieldDeclaration> reactiveFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(observedFields, _function_3);
        StringConcatenation _builder = new StringConcatenation();
        String _docComment = cls.getDocComment();
        _builder.append(_docComment, "");
        _builder.newLineIfNotEmpty();
        _builder.append("Part of this class source code is autogenerated by the @Entity active annotation.");
        _builder.newLine();
        _builder.append("Please see @Entity, EntityObject and ReactiveObject for more information on EntityObjects and ReactiveObjects.");
        _builder.newLine();
        _builder.append("<p>Detected reactive entity fields: ");
        final Function1<MutableFieldDeclaration, String> _function_4 = new Function1<MutableFieldDeclaration, String>() {
          public String apply(final MutableFieldDeclaration it) {
            return it.getSimpleName();
          }
        };
        Iterable<String> _map = org.eclipse.xtext.xbase.lib.IterableExtensions.map(reactiveFields, _function_4);
        _builder.append(_map, "");
        _builder.newLineIfNotEmpty();
        _builder.append("<p>Observing fields: ");
        final Function1<MutableFieldDeclaration, String> _function_5 = new Function1<MutableFieldDeclaration, String>() {
          public String apply(final MutableFieldDeclaration it) {
            return it.getSimpleName();
          }
        };
        Iterable<String> _map_1 = org.eclipse.xtext.xbase.lib.IterableExtensions.map(observedFields, _function_5);
        _builder.append(_map_1, "");
        _builder.append(" ");
        _builder.newLineIfNotEmpty();
        cls.setDocComment(_builder.toString());
        for (final MutableFieldDeclaration field : reactiveFields) {
          String _stopObservingFunctionName = this.getStopObservingFunctionName(field);
          final Procedure1<MutableFieldDeclaration> _function_6 = new Procedure1<MutableFieldDeclaration>() {
            public void apply(final MutableFieldDeclaration it) {
              it.setType(stopObservingType);
              it.setVisibility(Visibility.PROTECTED);
              it.setTransient(true);
            }
          };
          cls.addField(_stopObservingFunctionName, _function_6);
        }
        final Procedure1<MutableConstructorDeclaration> _function_7 = new Procedure1<MutableConstructorDeclaration>() {
          public void apply(final MutableConstructorDeclaration it) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Create an empty constructor for builders, eg:");
            _builder.newLine();
            _builder.append("<pre>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("val entity = new ");
            String _simpleName = cls.getSimpleName();
            _builder.append(_simpleName, "\t");
            _builder.append(" => [");
            _builder.newLineIfNotEmpty();
            {
              Iterable<? extends MutableMemberDeclaration> _declaredMembers = cls.getDeclaredMembers();
              int _length = ((Object[])Conversions.unwrapArray(_declaredMembers, Object.class)).length;
              boolean _greaterThan = (_length > 0);
              if (_greaterThan) {
                _builder.append("\t\t");
                Iterable<? extends MutableMemberDeclaration> _declaredMembers_1 = cls.getDeclaredMembers();
                MutableMemberDeclaration _head = org.eclipse.xtext.xbase.lib.IterableExtensions.head(_declaredMembers_1);
                String _simpleName_1 = _head.getSimpleName();
                _builder.append(_simpleName_1, "\t\t");
                _builder.append(" = \'test\'");
                _builder.newLineIfNotEmpty();
              }
            }
            _builder.append("\t");
            _builder.append("]");
            _builder.newLine();
            _builder.append("</pre>");
            _builder.newLine();
            it.setDocComment(_builder.toString());
            EntityProcessor.this.addClassTypeParameters(it, cls, context);
            final CompilationStrategy _function = new CompilationStrategy() {
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                return "";
              }
            };
            it.setBody(_function);
          }
        };
        cls.addConstructor(_function_7);
        int _length = ((Object[])Conversions.unwrapArray(requiredFields, Object.class)).length;
        boolean _greaterThan = (_length > 0);
        if (_greaterThan) {
          final Procedure1<MutableConstructorDeclaration> _function_8 = new Procedure1<MutableConstructorDeclaration>() {
            public void apply(final MutableConstructorDeclaration it) {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("Create a new ");
              String _simpleName = cls.getSimpleName();
              _builder.append(_simpleName, "");
              _builder.append(" for all fields annotated with @Require.");
              it.setDocComment(_builder.toString());
              EntityProcessor.this.addClassTypeParameters(it, cls, context);
              for (final MutableFieldDeclaration field : requiredFields) {
                String _simpleName_1 = field.getSimpleName();
                TypeReference _type = field.getType();
                it.addParameter(_simpleName_1, _type);
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
          cls.addConstructor(_function_8);
        }
        int _length_1 = ((Object[])Conversions.unwrapArray(observedFields, Object.class)).length;
        int _length_2 = ((Object[])Conversions.unwrapArray(requiredFields, Object.class)).length;
        boolean _greaterThan_1 = (_length_1 > _length_2);
        if (_greaterThan_1) {
          final Procedure1<MutableConstructorDeclaration> _function_9 = new Procedure1<MutableConstructorDeclaration>() {
            public void apply(final MutableConstructorDeclaration it) {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("Create a constructor for all fields (except for those annotated with @Ignore).");
              it.setDocComment(_builder.toString());
              EntityProcessor.this.addClassTypeParameters(it, cls, context);
              for (final MutableFieldDeclaration field : getSetFields) {
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
                    for(final MutableFieldDeclaration field : getSetFields) {
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
          cls.addConstructor(_function_9);
        }
        for (final MutableFieldDeclaration field_1 : observedFields) {
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
        final Procedure1<MutableMethodDeclaration> _function_10 = new Procedure1<MutableMethodDeclaration>() {
          public void apply(final MutableMethodDeclaration it) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Check if the ");
            String _simpleName = cls.getSimpleName();
            _builder.append(_simpleName, "");
            _builder.append(" is valid.");
            _builder.newLineIfNotEmpty();
            _builder.append("Also recursively checks contained entities within the members of ");
            String _simpleName_1 = cls.getSimpleName();
            _builder.append(_simpleName_1, "");
            _builder.append(".");
            _builder.newLineIfNotEmpty();
            _builder.append("@return true if all the fields annotated with @Require have a value.");
            _builder.newLine();
            it.setDocComment(_builder.toString());
            TypeReference _newTypeReference = context.newTypeReference("boolean");
            it.setReturnType(_newTypeReference);
            final CompilationStrategy _function = new CompilationStrategy() {
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                {
                  for(final MutableFieldDeclaration field : requiredFields) {
                    {
                      TypeReference _type = field.getType();
                      boolean _isPrimitive = _type.isPrimitive();
                      boolean _not = (!_isPrimitive);
                      if (_not) {
                        _builder.append("if(");
                        String _simpleName = field.getSimpleName();
                        _builder.append(_simpleName, "");
                        _builder.append("==null) return false;");
                        _builder.newLineIfNotEmpty();
                        {
                          boolean _in = IterableExtensions.<MutableFieldDeclaration>in(field, reactiveFields);
                          if (_in) {
                            _builder.append("if(!");
                            String _simpleName_1 = field.getSimpleName();
                            _builder.append(_simpleName_1, "");
                            _builder.append(".isValid()) return false;");
                            _builder.newLineIfNotEmpty();
                          }
                        }
                      }
                    }
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
        cls.addMethod("isValid", _function_10);
        for (final MutableFieldDeclaration f : getSetFields) {
          {
            String _simpleName = f.getSimpleName();
            String _firstUpper = StringExtensions.toFirstUpper(_simpleName);
            String _plus_1 = ("get" + _firstUpper);
            final Procedure1<MutableMethodDeclaration> _function_11 = new Procedure1<MutableMethodDeclaration>() {
              public void apply(final MutableMethodDeclaration it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("Get the value of the ");
                String _simpleName = cls.getSimpleName();
                _builder.append(_simpleName, "");
                _builder.append(" entity property ");
                String _simpleName_1 = f.getSimpleName();
                _builder.append(_simpleName_1, "");
                _builder.append(".");
                _builder.newLineIfNotEmpty();
                _builder.append("@return the found ");
                String _simpleName_2 = f.getSimpleName();
                _builder.append(_simpleName_2, "");
                _builder.append(" or null if not set.");
                _builder.newLineIfNotEmpty();
                it.setDocComment(_builder.toString());
                TypeReference _type = f.getType();
                String _simpleName_3 = _type.getSimpleName();
                Class<? extends Object> _get = typeConversions.get(_simpleName_3);
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
            cls.addMethod(_plus_1, _function_11);
            String _simpleName_1 = f.getSimpleName();
            String _firstUpper_1 = StringExtensions.toFirstUpper(_simpleName_1);
            String _plus_2 = ("set" + _firstUpper_1);
            final Procedure1<MutableMethodDeclaration> _function_12 = new Procedure1<MutableMethodDeclaration>() {
              public void apply(final MutableMethodDeclaration it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("Set the value of the ");
                String _simpleName = cls.getSimpleName();
                _builder.append(_simpleName, "");
                _builder.append(" entity property ");
                String _simpleName_1 = f.getSimpleName();
                _builder.append(_simpleName_1, "");
                _builder.append(".<p>");
                _builder.newLineIfNotEmpty();
                _builder.append("This will trigger a change event for the observers.");
                _builder.newLine();
                it.setDocComment(_builder.toString());
                TypeReference _type = f.getType();
                String _simpleName_2 = _type.getSimpleName();
                Class<? extends Object> _get = typeConversions.get(_simpleName_2);
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
                    {
                      boolean _in = IterableExtensions.<MutableFieldDeclaration>in(f, reactiveFields);
                      if (_in) {
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
                      }
                    }
                    _builder.append("// start observing the new value for changes");
                    _builder.newLine();
                    CharSequence _assignFieldValue = EntityProcessor.this.assignFieldValue(f, context);
                    _builder.append(_assignFieldValue, "");
                    _builder.newLineIfNotEmpty();
                    {
                      boolean _in_1 = IterableExtensions.<MutableFieldDeclaration>in(f, observedFields);
                      if (_in_1) {
                        _builder.append("// if we are publishing, publish the change we\'ve made");
                        _builder.newLine();
                        _builder.append("if(this.isPublishing()) {");
                        _builder.newLine();
                        {
                          boolean _or = false;
                          boolean _or_1 = false;
                          TypeReference _type = f.getType();
                          boolean _isPrimitive = _type.isPrimitive();
                          if (_isPrimitive) {
                            _or_1 = true;
                          } else {
                            Collection<Class<? extends Object>> _values = typeConversions.values();
                            final Function1<Class<? extends Object>, TypeReference> _function = new Function1<Class<? extends Object>, TypeReference>() {
                              public TypeReference apply(final Class<? extends Object> it) {
                                return context.newTypeReference(it);
                              }
                            };
                            Iterable<TypeReference> _map = org.eclipse.xtext.xbase.lib.IterableExtensions.<Class<? extends Object>, TypeReference>map(_values, _function);
                            List<TypeReference> _list = IterableExtensions.<TypeReference>toList(_map);
                            TypeReference _type_1 = f.getType();
                            boolean _contains = _list.contains(_type_1);
                            _or_1 = _contains;
                          }
                          if (_or_1) {
                            _or = true;
                          } else {
                            TypeReference _type_2 = f.getType();
                            TypeReference _newTypeReference = context.newTypeReference(String.class);
                            boolean _isAssignableFrom = _type_2.isAssignableFrom(_newTypeReference);
                            _or = _isAssignableFrom;
                          }
                          if (_or) {
                            _builder.append("\t");
                            _builder.append("getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, \"");
                            String _simpleName_1 = f.getSimpleName();
                            _builder.append(_simpleName_1, "\t");
                            _builder.append("\", value));");
                            _builder.newLineIfNotEmpty();
                          } else {
                            boolean _isEntityMap = EntityProcessor.this.isEntityMap(f);
                            if (_isEntityMap) {
                              _builder.append("\t");
                              _builder.append("getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, \"");
                              String _simpleName_2 = f.getSimpleName();
                              _builder.append(_simpleName_2, "\t");
                              _builder.append("\", ((");
                              TypeReference _entityMapType = EntityProcessor.this.toEntityMapType(f, context);
                              String _name = _entityMapType.getName();
                              _builder.append(_name, "\t");
                              _builder.append(")this.");
                              String _simpleName_3 = f.getSimpleName();
                              _builder.append(_simpleName_3, "\t");
                              _builder.append(").clone()));");
                              _builder.newLineIfNotEmpty();
                            } else {
                              boolean _isEntityList = EntityProcessor.this.isEntityList(f);
                              if (_isEntityList) {
                                _builder.append("\t");
                                _builder.append("getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, \"");
                                String _simpleName_4 = f.getSimpleName();
                                _builder.append(_simpleName_4, "\t");
                                _builder.append("\", ((");
                                TypeReference _entityListType = EntityProcessor.this.toEntityListType(f, context);
                                String _name_1 = _entityListType.getName();
                                _builder.append(_name_1, "\t");
                                _builder.append(")this.");
                                String _simpleName_5 = f.getSimpleName();
                                _builder.append(_simpleName_5, "\t");
                                _builder.append(").clone()));");
                                _builder.newLineIfNotEmpty();
                              } else {
                                _builder.append("\t");
                                _builder.append("getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, \"");
                                String _simpleName_6 = f.getSimpleName();
                                _builder.append(_simpleName_6, "\t");
                                _builder.append("\", this.");
                                String _simpleName_7 = f.getSimpleName();
                                _builder.append(_simpleName_7, "\t");
                                _builder.append(".clone()));");
                                _builder.newLineIfNotEmpty();
                              }
                            }
                          }
                        }
                        _builder.append("}");
                        _builder.newLine();
                      }
                    }
                    return _builder;
                  }
                };
                it.setBody(_function);
              }
            };
            cls.addMethod(_plus_2, _function_12);
          }
        }
        final Procedure1<MutableMethodDeclaration> _function_11 = new Procedure1<MutableMethodDeclaration>() {
          public void apply(final MutableMethodDeclaration it) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Apply a change to the ");
            String _simpleName = cls.getSimpleName();
            _builder.append(_simpleName, "");
            _builder.append(".<br/>");
            _builder.newLineIfNotEmpty();
            _builder.append("This will not trigger a change for observers of this ");
            String _simpleName_1 = cls.getSimpleName();
            _builder.append(_simpleName_1, "");
            _builder.append(".");
            _builder.newLineIfNotEmpty();
            _builder.append("<p>");
            _builder.newLine();
            _builder.append("The change is not neccessarly applied to this object itself, ");
            _builder.newLine();
            _builder.append("but depending on the path of the change, be applied to members or members of members.");
            _builder.newLine();
            it.setDocComment(_builder.toString());
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
                  for(final MutableFieldDeclaration field : observedFields) {
                    {
                      TypeReference _type = field.getType();
                      boolean _isPrimitive = _type.isPrimitive();
                      boolean _not = (!_isPrimitive);
                      if (_not) {
                        _builder.append("\t\t");
                        _builder.append("if(value.");
                        String _simpleName = field.getSimpleName();
                        _builder.append(_simpleName, "\t\t");
                        _builder.append(" != null) ");
                        _builder.newLineIfNotEmpty();
                      }
                    }
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
                  for(final MutableFieldDeclaration field_1 : observedFields) {
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
                    TypeReference _type_1 = field_1.getType();
                    String _simpleName_5 = _type_1.getSimpleName();
                    _builder.append(_simpleName_5, "\t\t\t\t\t");
                    _builder.append(")change.getValue());");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t\t\t");
                    _builder.append("// this.");
                    String _simpleName_6 = field_1.getSimpleName();
                    _builder.append(_simpleName_6, "\t\t\t\t\t");
                    _builder.append(" = (");
                    TypeReference _type_2 = field_1.getType();
                    String _simpleName_7 = _type_2.getSimpleName();
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
                    {
                      TypeReference _type_3 = field_1.getType();
                      boolean _isPrimitive_1 = _type_3.isPrimitive();
                      boolean _not_1 = (!_isPrimitive_1);
                      if (_not_1) {
                        _builder.append("\t\t");
                        _builder.append("\t\t\t");
                        _builder.append("this.");
                        String _simpleName_8 = field_1.getSimpleName();
                        _builder.append(_simpleName_8, "\t\t\t\t\t");
                        _builder.append(" = null;");
                        _builder.newLineIfNotEmpty();
                      }
                    }
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
                  for(final MutableFieldDeclaration field_2 : observedFields) {
                    _builder.append("\t\t");
                    _builder.append("if(field.equals(\"");
                    String _simpleName_11 = field_2.getSimpleName();
                    _builder.append(_simpleName_11, "\t\t");
                    _builder.append("\")) {");
                    _builder.newLineIfNotEmpty();
                    {
                      boolean _isReactive = EntityProcessor.this.isReactive(field_2, context);
                      if (_isReactive) {
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
        cls.addMethod("apply", _function_11);
        final Procedure1<MutableMethodDeclaration> _function_12 = new Procedure1<MutableMethodDeclaration>() {
          public void apply(final MutableMethodDeclaration it) {
            context.setPrimarySourceElement(it, cls);
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
                  for(final MutableFieldDeclaration field : getSetFields) {
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
        cls.addMethod("toString", _function_12);
        Iterable<? extends MutableMethodDeclaration> _declaredMethods = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_13 = new Function1<MutableMethodDeclaration, Boolean>() {
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "equals"));
          }
        };
        MutableMethodDeclaration _findFirst = org.eclipse.xtext.xbase.lib.IterableExtensions.findFirst(_declaredMethods, _function_13);
        boolean _defined = OptExtensions.<Object>defined(_findFirst);
        boolean _not = (!_defined);
        if (_not) {
          final Procedure1<MutableMethodDeclaration> _function_14 = new Procedure1<MutableMethodDeclaration>() {
            public void apply(final MutableMethodDeclaration it) {
              context.setPrimarySourceElement(it, cls);
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
                      {
                        TypeReference _type = field.getType();
                        boolean _isPrimitive = _type.isPrimitive();
                        if (_isPrimitive) {
                          _builder.append("\t\t");
                          _builder.append("this.");
                          String _simpleName_1 = field.getSimpleName();
                          _builder.append(_simpleName_1, "\t\t");
                          _builder.append(" == ");
                          String _simpleName_2 = field.getSimpleName();
                          _builder.append(_simpleName_2, "\t\t");
                          _builder.newLineIfNotEmpty();
                        } else {
                          _builder.append("\t\t");
                          _builder.append("(");
                          _builder.newLine();
                          _builder.append("\t\t");
                          _builder.append("\t");
                          _builder.append("(this.");
                          String _simpleName_3 = field.getSimpleName();
                          _builder.append(_simpleName_3, "\t\t\t");
                          _builder.append(" == null && ((");
                          String _simpleName_4 = cls.getSimpleName();
                          _builder.append(_simpleName_4, "\t\t\t");
                          _builder.append(") object).");
                          String _simpleName_5 = field.getSimpleName();
                          _builder.append(_simpleName_5, "\t\t\t");
                          _builder.append(" == null) ||");
                          _builder.newLineIfNotEmpty();
                          _builder.append("\t\t");
                          _builder.append("\t");
                          _builder.append("(");
                          _builder.newLine();
                          _builder.append("\t\t");
                          _builder.append("\t\t");
                          _builder.append("this.");
                          String _simpleName_6 = field.getSimpleName();
                          _builder.append(_simpleName_6, "\t\t\t\t");
                          _builder.append(" != null && ");
                          _builder.newLineIfNotEmpty();
                          _builder.append("\t\t");
                          _builder.append("\t\t");
                          _builder.append("this.");
                          String _simpleName_7 = field.getSimpleName();
                          _builder.append(_simpleName_7, "\t\t\t\t");
                          _builder.append(".equals(((");
                          String _simpleName_8 = cls.getSimpleName();
                          _builder.append(_simpleName_8, "\t\t\t\t");
                          _builder.append(") object).");
                          String _simpleName_9 = field.getSimpleName();
                          _builder.append(_simpleName_9, "\t\t\t\t");
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
          cls.addMethod("equals", _function_14);
        }
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_1 = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_15 = new Function1<MutableMethodDeclaration, Boolean>() {
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "hashCode"));
          }
        };
        MutableMethodDeclaration _findFirst_1 = org.eclipse.xtext.xbase.lib.IterableExtensions.findFirst(_declaredMethods_1, _function_15);
        boolean _defined_1 = OptExtensions.<Object>defined(_findFirst_1);
        boolean _not_1 = (!_defined_1);
        if (_not_1) {
          final Procedure1<MutableMethodDeclaration> _function_16 = new Procedure1<MutableMethodDeclaration>() {
            public void apply(final MutableMethodDeclaration it) {
              context.setPrimarySourceElement(it, cls);
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
                      {
                        TypeReference _type = field.getType();
                        boolean _isPrimitive = _type.isPrimitive();
                        if (_isPrimitive) {
                          _builder.append("\t");
                          _builder.append("(this.");
                          String _simpleName = field.getSimpleName();
                          _builder.append(_simpleName, "\t");
                          _builder.append(" + \"\").hashCode()");
                          _builder.newLineIfNotEmpty();
                        } else {
                          _builder.append("\t");
                          _builder.append("((this.");
                          String _simpleName_1 = field.getSimpleName();
                          _builder.append(_simpleName_1, "\t");
                          _builder.append(" != null) ?");
                          _builder.newLineIfNotEmpty();
                          _builder.append("\t");
                          _builder.append("\t");
                          _builder.append("(this.");
                          String _simpleName_2 = field.getSimpleName();
                          _builder.append(_simpleName_2, "\t\t");
                          _builder.append(" + \"\").hashCode()");
                          _builder.newLineIfNotEmpty();
                          _builder.append("\t");
                          _builder.append("\t");
                          _builder.append(": 0)");
                          _builder.newLine();
                        }
                      }
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
          cls.addMethod("hashCode", _function_16);
        }
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_2 = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_17 = new Function1<MutableMethodDeclaration, Boolean>() {
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "clone"));
          }
        };
        MutableMethodDeclaration _findFirst_2 = org.eclipse.xtext.xbase.lib.IterableExtensions.findFirst(_declaredMethods_2, _function_17);
        boolean _defined_2 = OptExtensions.<Object>defined(_findFirst_2);
        boolean _not_2 = (!_defined_2);
        if (_not_2) {
          final Procedure1<MutableMethodDeclaration> _function_18 = new Procedure1<MutableMethodDeclaration>() {
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
          cls.addMethod("clone", _function_18);
        }
        final Function1<MutableFieldDeclaration, Boolean> _function_19 = new Function1<MutableFieldDeclaration, Boolean>() {
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            String _simpleName = _type.getSimpleName();
            return Boolean.valueOf(_simpleName.startsWith("Map"));
          }
        };
        Iterable<? extends MutableFieldDeclaration> _filter = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(reactiveFields, _function_19);
        final Procedure1<MutableFieldDeclaration> _function_20 = new Procedure1<MutableFieldDeclaration>() {
          public void apply(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
            final TypeReference key = _actualTypeArguments.get(0);
            TypeReference _type_1 = it.getType();
            List<TypeReference> _actualTypeArguments_1 = _type_1.getActualTypeArguments();
            final TypeReference value = _actualTypeArguments_1.get(1);
            TypeReference _string = context.getString();
            boolean _extendsClass = EntityProcessor.this.<Object>extendsClass(key, _string);
            boolean _not = (!_extendsClass);
            if (_not) {
              context.addError(it, "Maps in EntityObjects may only have String as their key");
            } else {
              TypeReference _newTypeReference = context.newTypeReference(EntityMap.class, value);
              it.setType(_newTypeReference);
            }
          }
        };
        IterableExtensions.<MutableFieldDeclaration>each(_filter, _function_20);
        final Function1<MutableFieldDeclaration, Boolean> _function_21 = new Function1<MutableFieldDeclaration, Boolean>() {
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            String _simpleName = _type.getSimpleName();
            return Boolean.valueOf(_simpleName.startsWith("List"));
          }
        };
        Iterable<? extends MutableFieldDeclaration> _filter_1 = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(reactiveFields, _function_21);
        final Procedure1<MutableFieldDeclaration> _function_22 = new Procedure1<MutableFieldDeclaration>() {
          public void apply(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
            final TypeReference typeArg = _actualTypeArguments.get(0);
            TypeReference _newTypeReference = context.newTypeReference(EntityList.class, typeArg);
            it.setType(_newTypeReference);
          }
        };
        IterableExtensions.<MutableFieldDeclaration>each(_filter_1, _function_22);
        final Procedure1<MutableMethodDeclaration> _function_23 = new Procedure1<MutableMethodDeclaration>() {
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
        cls.addMethod("newChangeHandler", _function_23);
      }
    }
  }
  
  public boolean isReactive(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    boolean _or = false;
    boolean _or_1 = false;
    TypeReference _type = field.getType();
    TypeReference _newTypeReference = context.newTypeReference(ReactiveObject.class);
    boolean _extendsClass = this.<Object>extendsClass(_type, _newTypeReference);
    if (_extendsClass) {
      _or_1 = true;
    } else {
      TypeReference _type_1 = field.getType();
      TypeReference _newTypeReference_1 = context.newTypeReference(List.class);
      boolean _extendsClass_1 = this.<Object>extendsClass(_type_1, _newTypeReference_1);
      _or_1 = _extendsClass_1;
    }
    if (_or_1) {
      _or = true;
    } else {
      TypeReference _type_2 = field.getType();
      TypeReference _newTypeReference_2 = context.newTypeReference(Map.class);
      boolean _extendsClass_2 = this.<Object>extendsClass(_type_2, _newTypeReference_2);
      _or = _extendsClass_2;
    }
    return _or;
  }
  
  public boolean isEntityList(final MutableFieldDeclaration field) {
    TypeReference _type = field.getType();
    String _simpleName = _type.getSimpleName();
    return _simpleName.startsWith("EntityList");
  }
  
  public boolean isEntityMap(final MutableFieldDeclaration field) {
    TypeReference _type = field.getType();
    String _simpleName = _type.getSimpleName();
    return _simpleName.startsWith("EntityMap");
  }
  
  public boolean isObservable(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    TypeReference _newTypeReference = context.newTypeReference(Change.class);
    TypeReference _newTypeReference_1 = context.newTypeReference(Observable.class, _newTypeReference);
    TypeReference _type = field.getType();
    return _newTypeReference_1.isAssignableFrom(_type);
  }
  
  public <T extends Object> boolean extendsClass(final TypeReference type, final TypeReference superType) {
    return superType.isAssignableFrom(type);
  }
  
  public CharSequence assignFieldValue(final MutableFieldDeclaration f, @Extension final TransformationContext context) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _isEntityList = this.isEntityList(f);
      if (_isEntityList) {
        _builder.append("// if the list is not already reactive, wrap the list as a reactive list");
        _builder.newLine();
        _builder.append("if(");
        String _simpleName = f.getSimpleName();
        _builder.append(_simpleName, "");
        _builder.append(" == null || !(");
        String _simpleName_1 = f.getSimpleName();
        _builder.append(_simpleName_1, "");
        _builder.append(" instanceof  nl.kii.entity.EntityList<?>)) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        TypeReference _type = f.getType();
        List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
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
        String _simpleName_2 = f.getSimpleName();
        _builder.append(_simpleName_2, "\t");
        _builder.append(" != null) newList.addAll(");
        String _simpleName_3 = f.getSimpleName();
        _builder.append(_simpleName_3, "\t");
        _builder.append(");");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        String _simpleName_4 = f.getSimpleName();
        _builder.append(_simpleName_4, "\t");
        _builder.append(" = newList;");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("this.");
        String _stopObservingFunctionName = this.getStopObservingFunctionName(f);
        _builder.append(_stopObservingFunctionName, "\t");
        _builder.append(" = newList.onChange(newChangeHandler(\"");
        String _simpleName_5 = f.getSimpleName();
        _builder.append(_simpleName_5, "\t");
        _builder.append("\"));");
        _builder.newLineIfNotEmpty();
        _builder.append("}");
        _builder.newLine();
      } else {
        boolean _isEntityMap = this.isEntityMap(f);
        if (_isEntityMap) {
          _builder.append("// if the map is not already listenable, wrap the map as a listenable");
          _builder.newLine();
          _builder.append("if(");
          String _simpleName_6 = f.getSimpleName();
          _builder.append(_simpleName_6, "");
          _builder.append(" == null || !(");
          String _simpleName_7 = f.getSimpleName();
          _builder.append(_simpleName_7, "");
          _builder.append(" instanceof  nl.kii.entity.EntityMap<?>)) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          TypeReference _type_1 = f.getType();
          List<TypeReference> _actualTypeArguments_1 = _type_1.getActualTypeArguments();
          final TypeReference typeArg_1 = _actualTypeArguments_1.get(0);
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
          String _simpleName_8 = typeArg_1.getSimpleName();
          _builder.append(_simpleName_8, "\t");
          _builder.append(".class);");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("if(");
          String _simpleName_9 = f.getSimpleName();
          _builder.append(_simpleName_9, "\t");
          _builder.append(" != null) newMap.putAll(");
          String _simpleName_10 = f.getSimpleName();
          _builder.append(_simpleName_10, "\t");
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          String _simpleName_11 = f.getSimpleName();
          _builder.append(_simpleName_11, "\t");
          _builder.append(" = newMap;");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("this.");
          String _stopObservingFunctionName_1 = this.getStopObservingFunctionName(f);
          _builder.append(_stopObservingFunctionName_1, "\t");
          _builder.append(" = newMap.onChange(newChangeHandler(\"");
          String _simpleName_12 = f.getSimpleName();
          _builder.append(_simpleName_12, "\t");
          _builder.append("\"));");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        } else {
          boolean _isObservable = this.isObservable(f, context);
          if (_isObservable) {
            _builder.append("this.");
            String _simpleName_13 = f.getSimpleName();
            _builder.append(_simpleName_13, "");
            _builder.append(" = value;");
            _builder.newLineIfNotEmpty();
            _builder.append("this.");
            String _stopObservingFunctionName_2 = this.getStopObservingFunctionName(f);
            _builder.append(_stopObservingFunctionName_2, "");
            _builder.append(" = this.");
            String _simpleName_14 = f.getSimpleName();
            _builder.append(_simpleName_14, "");
            _builder.append(".onChange(newChangeHandler(\"");
            String _simpleName_15 = f.getSimpleName();
            _builder.append(_simpleName_15, "");
            _builder.append("\"));");
            _builder.newLineIfNotEmpty();
          } else {
            _builder.append("this.");
            String _simpleName_16 = f.getSimpleName();
            _builder.append(_simpleName_16, "");
            _builder.append(" = value;");
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
    TypeReference _get = _actualTypeArguments.get(0);
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
          list.add(_builder.toString());
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
          list.add(_typeParamName);
        }
      };
      org.eclipse.xtext.xbase.lib.IterableExtensions.forEach(_typeParameters, _function);
      _xblockexpression = list;
    }
    return _xblockexpression;
  }
}
