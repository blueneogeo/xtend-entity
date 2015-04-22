package nl.kii.entity.processors;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import nl.kii.entity.Change;
import nl.kii.entity.EntityException;
import nl.kii.entity.EntityList;
import nl.kii.entity.EntityMap;
import nl.kii.entity.EntityObject;
import nl.kii.entity.ReactiveObject;
import nl.kii.entity.annotations.Entity;
import nl.kii.entity.annotations.Ignore;
import nl.kii.entity.annotations.Require;
import nl.kii.observe.Observable;
import nl.kii.util.IterableExtensions;
import nl.kii.util.OptExtensions;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.TransformationParticipant;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.AnnotationTarget;
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
  @Override
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
        context.setPrimarySourceElement(cls, cls);
        TypeReference _newTypeReference_1 = context.newTypeReference(ReactiveObject.class);
        cls.setExtendedClass(_newTypeReference_1);
        Iterable<? extends TypeReference> _implementedInterfaces = cls.getImplementedInterfaces();
        TypeReference _newTypeReference_2 = context.newTypeReference(Cloneable.class);
        Iterable<TypeReference> _plus = Iterables.<TypeReference>concat(_implementedInterfaces, Collections.<TypeReference>unmodifiableList(CollectionLiterals.<TypeReference>newArrayList(_newTypeReference_2)));
        cls.setImplementedInterfaces(_plus);
        Iterable<? extends MutableFieldDeclaration> _declaredFields = cls.getDeclaredFields();
        final Function1<MutableFieldDeclaration, Boolean> _function = new Function1<MutableFieldDeclaration, Boolean>() {
          @Override
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
          @Override
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _newTypeReference = context.newTypeReference(Require.class);
            Type _type = _newTypeReference.getType();
            AnnotationReference _findAnnotation = it.findAnnotation(_type);
            return Boolean.valueOf((!Objects.equal(_findAnnotation, null)));
          }
        };
        final Iterable<? extends MutableFieldDeclaration> requiredFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(getSetFields, _function_1);
        final Function1<MutableFieldDeclaration, Boolean> _function_2 = new Function1<MutableFieldDeclaration, Boolean>() {
          @Override
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _newTypeReference = context.newTypeReference(Ignore.class);
            Type _type = _newTypeReference.getType();
            AnnotationReference _findAnnotation = it.findAnnotation(_type);
            return Boolean.valueOf(Objects.equal(_findAnnotation, null));
          }
        };
        final Iterable<? extends MutableFieldDeclaration> observedFields = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(getSetFields, _function_2);
        final Function1<MutableFieldDeclaration, Boolean> _function_3 = new Function1<MutableFieldDeclaration, Boolean>() {
          @Override
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
          @Override
          public String apply(final MutableFieldDeclaration it) {
            return it.getSimpleName();
          }
        };
        Iterable<String> _map = org.eclipse.xtext.xbase.lib.IterableExtensions.map(reactiveFields, _function_4);
        _builder.append(_map, "");
        _builder.newLineIfNotEmpty();
        _builder.append("<p>Observing fields: ");
        final Function1<MutableFieldDeclaration, String> _function_5 = new Function1<MutableFieldDeclaration, String>() {
          @Override
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
            @Override
            public void apply(final MutableFieldDeclaration it) {
              it.setType(stopObservingType);
              context.setPrimarySourceElement(it, field);
              it.setVisibility(Visibility.PROTECTED);
              it.setTransient(true);
            }
          };
          cls.addField(_stopObservingFunctionName, _function_6);
        }
        final Procedure1<MutableConstructorDeclaration> _function_7 = new Procedure1<MutableConstructorDeclaration>() {
          @Override
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
            context.setPrimarySourceElement(it, cls);
            EntityProcessor.this.addClassTypeParameters(it, cls, context);
            final CompilationStrategy _function = new CompilationStrategy() {
              @Override
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
            @Override
            public void apply(final MutableConstructorDeclaration it) {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("Create a new ");
              String _simpleName = cls.getSimpleName();
              _builder.append(_simpleName, "");
              _builder.append(" for all fields annotated with @Require.");
              it.setDocComment(_builder.toString());
              context.setPrimarySourceElement(it, cls);
              EntityProcessor.this.addClassTypeParameters(it, cls, context);
              for (final MutableFieldDeclaration field : requiredFields) {
                String _simpleName_1 = field.getSimpleName();
                TypeReference _type = field.getType();
                it.addParameter(_simpleName_1, _type);
              }
              final CompilationStrategy _function = new CompilationStrategy() {
                @Override
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
            @Override
            public void apply(final MutableConstructorDeclaration it) {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("Create a constructor for all fields (except for those annotated with @Ignore).");
              it.setDocComment(_builder.toString());
              context.setPrimarySourceElement(it, cls);
              EntityProcessor.this.addClassTypeParameters(it, cls, context);
              for (final MutableFieldDeclaration field : getSetFields) {
                String _simpleName = field.getSimpleName();
                TypeReference _type = field.getType();
                it.addParameter(_simpleName, _type);
              }
              final CompilationStrategy _function = new CompilationStrategy() {
                @Override
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
          @Override
          public void apply(final MutableMethodDeclaration it) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Gives the instance access to the getType method.");
            it.setDocComment(_builder.toString());
            context.setPrimarySourceElement(it, cls);
            TypeReference _string = context.getString();
            TypeReference _newTypeReference = context.newTypeReference(List.class, _string);
            it.addParameter("path", _newTypeReference);
            TypeReference _newTypeReference_1 = context.newTypeReference(Class.class);
            it.setReturnType(_newTypeReference_1);
            TypeReference _newTypeReference_2 = context.newTypeReference(EntityException.class);
            it.setExceptions(_newTypeReference_2);
            final CompilationStrategy _function = new CompilationStrategy() {
              @Override
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("return getType(path);");
                _builder.newLine();
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addMethod("getInstanceType", _function_10);
        final Procedure1<MutableMethodDeclaration> _function_11 = new Procedure1<MutableMethodDeclaration>() {
          @Override
          public void apply(final MutableMethodDeclaration it) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Gets the class of any field path into the object. Also navigates inner maps and lists.");
            _builder.newLine();
            _builder.append("This lets you get past erasure, and look into the wrapped types of objects at runtime.");
            _builder.newLine();
            _builder.append("<p>");
            _builder.newLine();
            _builder.append("The path is made up of a strings, each the name of a field. An empty path will give the");
            _builder.newLine();
            _builder.append("type of this object, while a single string will give the type of that field inside this");
            _builder.newLine();
            _builder.append("class. More strings will navigate recursively into that type. ");
            _builder.newLine();
            _builder.append("<p>");
            _builder.newLine();
            _builder.append("For instance, if you have an entity with a field users that is a Map<String, User>, ");
            _builder.newLine();
            _builder.append("and each user has a name field of type String, then you could get the type of that name ");
            _builder.newLine();
            _builder.append("field by asking the entity:");
            _builder.newLine();
            _builder.append("<pre>Entity.getType(\'users\', \'john\', \'name\') // returns String</pre>");
            _builder.newLine();
            it.setDocComment(_builder.toString());
            context.setPrimarySourceElement(it, cls);
            it.setStatic(true);
            TypeReference _string = context.getString();
            TypeReference _newTypeReference = context.newTypeReference(List.class, _string);
            it.addParameter("path", _newTypeReference);
            TypeReference _newTypeReference_1 = context.newTypeReference(Class.class);
            it.setReturnType(_newTypeReference_1);
            TypeReference _newTypeReference_2 = context.newTypeReference(EntityException.class);
            it.setExceptions(_newTypeReference_2);
            final CompilationStrategy _function = new CompilationStrategy() {
              @Override
              public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("if(path == null || path.size() == 0) return ");
                String _nameWithoutGenerics = EntityProcessor.this.getNameWithoutGenerics(clsType);
                _builder.append(_nameWithoutGenerics, "");
                _builder.append(".class;");
                _builder.newLineIfNotEmpty();
                _builder.append("String fieldName = path.get(0);");
                _builder.newLine();
                {
                  for(final MutableFieldDeclaration field : getSetFields) {
                    _builder.append("if(fieldName.equals(\"");
                    String _simpleName = field.getSimpleName();
                    _builder.append(_simpleName, "");
                    _builder.append("\")) {");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t");
                    _builder.append("if(path.size() == 1) {");
                    _builder.newLine();
                    _builder.append("\t\t\t");
                    _builder.append("return ");
                    TypeReference _type = field.getType();
                    String _nameWithoutGenerics_1 = EntityProcessor.this.getNameWithoutGenerics(_type);
                    _builder.append(_nameWithoutGenerics_1, "\t\t\t");
                    _builder.append(".class;");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t");
                    _builder.append("} else {");
                    _builder.newLine();
                    {
                      boolean _or = false;
                      TypeReference _type_1 = field.getType();
                      TypeReference _newTypeReference = context.newTypeReference(EntityList.class);
                      boolean _extendsType = EntityProcessor.this.<Object>extendsType(_type_1, _newTypeReference);
                      if (_extendsType) {
                        _or = true;
                      } else {
                        TypeReference _type_2 = field.getType();
                        TypeReference _newTypeReference_1 = context.newTypeReference(EntityMap.class);
                        boolean _extendsType_1 = EntityProcessor.this.<Object>extendsType(_type_2, _newTypeReference_1);
                        _or = _extendsType_1;
                      }
                      if (_or) {
                        _builder.append("\t\t");
                        TypeReference _type_3 = field.getType();
                        List<TypeReference> _actualTypeArguments = _type_3.getActualTypeArguments();
                        final TypeReference containedType = _actualTypeArguments.get(0);
                        _builder.newLineIfNotEmpty();
                        _builder.append("\t\t");
                        _builder.append("if(path.size() == 2) return ");
                        _builder.append(containedType, "\t\t");
                        _builder.append(".class;");
                        _builder.newLineIfNotEmpty();
                        _builder.append("\t\t");
                        _builder.append("else ");
                        _builder.newLine();
                        {
                          TypeReference _newTypeReference_2 = context.newTypeReference(EntityObject.class);
                          boolean _extendsType_2 = EntityProcessor.this.<Object>extendsType(containedType, _newTypeReference_2);
                          if (_extendsType_2) {
                            _builder.append("\t\t");
                            _builder.append("return ");
                            _builder.append(containedType, "\t\t");
                            _builder.append(".getType(path.subList(2, path.size()));");
                            _builder.newLineIfNotEmpty();
                          } else {
                            _builder.append("\t\t");
                            _builder.append("throw new EntityException(\"path \" + path + \" does not match structure of ");
                            String _simpleName_1 = containedType.getSimpleName();
                            _builder.append(_simpleName_1, "\t\t");
                            _builder.append("\");");
                            _builder.newLineIfNotEmpty();
                          }
                        }
                      } else {
                        TypeReference _type_4 = field.getType();
                        TypeReference _newTypeReference_3 = context.newTypeReference(EntityObject.class);
                        boolean _extendsType_3 = EntityProcessor.this.<Object>extendsType(_type_4, _newTypeReference_3);
                        if (_extendsType_3) {
                          _builder.append("\t\t");
                          _builder.append("return ");
                          TypeReference _type_5 = field.getType();
                          String _simpleName_2 = _type_5.getSimpleName();
                          _builder.append(_simpleName_2, "\t\t");
                          _builder.append(".getType(path.subList(1, path.size()));");
                          _builder.newLineIfNotEmpty();
                        } else {
                          _builder.append("\t\t");
                          _builder.append("throw new EntityException(\"path \" + path + \" does not match structure of ");
                          TypeReference _type_6 = field.getType();
                          String _simpleName_3 = _type_6.getSimpleName();
                          _builder.append(_simpleName_3, "\t\t");
                          _builder.append("\"); ");
                          _builder.newLineIfNotEmpty();
                        }
                      }
                    }
                    _builder.append("\t");
                    _builder.append("} ");
                    _builder.newLine();
                    _builder.append("}");
                    _builder.newLine();
                  }
                }
                _builder.append("throw new EntityException(\"could not match path \" + path + \" on entity ");
                String _simpleName_4 = clsType.getSimpleName();
                _builder.append(_simpleName_4, "");
                _builder.append("\");");
                _builder.newLineIfNotEmpty();
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addMethod("getType", _function_11);
        final Procedure1<MutableMethodDeclaration> _function_12 = new Procedure1<MutableMethodDeclaration>() {
          @Override
          public void apply(final MutableMethodDeclaration it) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Only returns true if ");
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
            context.setPrimarySourceElement(it, cls);
            TypeReference _newTypeReference = context.newTypeReference(boolean.class);
            it.setReturnType(_newTypeReference);
            final CompilationStrategy _function = new CompilationStrategy() {
              @Override
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
                            _builder.append("\t");
                            _builder.append("if(!");
                            String _simpleName_1 = field.getSimpleName();
                            _builder.append(_simpleName_1, "\t");
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
        cls.addMethod("isValid", _function_12);
        final Procedure1<MutableMethodDeclaration> _function_13 = new Procedure1<MutableMethodDeclaration>() {
          @Override
          public void apply(final MutableMethodDeclaration it) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Check if the ");
            String _simpleName = cls.getSimpleName();
            _builder.append(_simpleName, "");
            _builder.append(" is valid.");
            _builder.newLineIfNotEmpty();
            _builder.append("Use this method if you need a descriptive error for what did not match.");
            _builder.newLine();
            _builder.append("@throws an exception if not all the fields annotated with @Require have a value.");
            _builder.newLine();
            it.setDocComment(_builder.toString());
            context.setPrimarySourceElement(it, cls);
            TypeReference _newTypeReference = context.newTypeReference(EntityException.class);
            it.setExceptions(_newTypeReference);
            final CompilationStrategy _function = new CompilationStrategy() {
              @Override
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
                        _builder.append("==null) throw new EntityException(\"");
                        String _simpleName_1 = cls.getSimpleName();
                        _builder.append(_simpleName_1, "");
                        _builder.append(".");
                        String _simpleName_2 = field.getSimpleName();
                        _builder.append(_simpleName_2, "");
                        _builder.append(" may not be empty.\");");
                        _builder.newLineIfNotEmpty();
                        {
                          boolean _in = IterableExtensions.<MutableFieldDeclaration>in(field, reactiveFields);
                          if (_in) {
                            String _simpleName_3 = field.getSimpleName();
                            _builder.append(_simpleName_3, "");
                            _builder.append(".validate();");
                            _builder.newLineIfNotEmpty();
                          }
                        }
                      }
                    }
                  }
                }
                return _builder;
              }
            };
            it.setBody(_function);
          }
        };
        cls.addMethod("validate", _function_13);
        for (final MutableFieldDeclaration f : getSetFields) {
          {
            String _simpleName = f.getSimpleName();
            String _firstUpper = StringExtensions.toFirstUpper(_simpleName);
            String _plus_1 = ("get" + _firstUpper);
            final Procedure1<MutableMethodDeclaration> _function_14 = new Procedure1<MutableMethodDeclaration>() {
              @Override
              public void apply(final MutableMethodDeclaration it) {
                StringConcatenation _builder = new StringConcatenation();
                {
                  String _docComment = f.getDocComment();
                  boolean _defined = OptExtensions.<Object>defined(_docComment);
                  if (_defined) {
                    String _docComment_1 = f.getDocComment();
                    _builder.append(_docComment_1, "");
                  } else {
                    _builder.append("Get the value of the ");
                    String _simpleName = cls.getSimpleName();
                    _builder.append(_simpleName, "");
                    _builder.append(" entity property ");
                    String _simpleName_1 = f.getSimpleName();
                    _builder.append(_simpleName_1, "");
                    _builder.append(".");
                  }
                }
                _builder.newLineIfNotEmpty();
                _builder.append("@return the found ");
                String _simpleName_2 = f.getSimpleName();
                _builder.append(_simpleName_2, "");
                _builder.append(" or null if not set.");
                _builder.newLineIfNotEmpty();
                it.setDocComment(_builder.toString());
                boolean _isDeprecated = f.isDeprecated();
                it.setDeprecated(_isDeprecated);
                context.setPrimarySourceElement(it, f);
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
                  @Override
                  public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                    StringConcatenation _builder = new StringConcatenation();
                    {
                      boolean _isEntityMap = EntityProcessor.this.isEntityMap(f);
                      if (_isEntityMap) {
                        _builder.append("if(");
                        String _simpleName = f.getSimpleName();
                        _builder.append(_simpleName, "");
                        _builder.append("==null) {");
                        _builder.newLineIfNotEmpty();
                        _builder.append("\t");
                        CharSequence _newEntityMap = EntityProcessor.this.newEntityMap(f, "newMap", context);
                        _builder.append(_newEntityMap, "\t");
                        _builder.newLineIfNotEmpty();
                        _builder.append("\t");
                        String _simpleName_1 = f.getSimpleName();
                        _builder.append(_simpleName_1, "\t");
                        _builder.append(" = newMap;");
                        _builder.newLineIfNotEmpty();
                        _builder.append("}");
                        _builder.newLine();
                      } else {
                        boolean _isEntityList = EntityProcessor.this.isEntityList(f);
                        if (_isEntityList) {
                          _builder.append("if(");
                          String _simpleName_2 = f.getSimpleName();
                          _builder.append(_simpleName_2, "");
                          _builder.append("==null) {");
                          _builder.newLineIfNotEmpty();
                          _builder.append("\t");
                          CharSequence _newEntityList = EntityProcessor.this.newEntityList(f, "newList", context);
                          _builder.append(_newEntityList, "\t");
                          _builder.newLineIfNotEmpty();
                          _builder.append("\t");
                          String _simpleName_3 = f.getSimpleName();
                          _builder.append(_simpleName_3, "\t");
                          _builder.append(" = newList;");
                          _builder.newLineIfNotEmpty();
                          _builder.append("}");
                          _builder.newLine();
                        }
                      }
                    }
                    _builder.append("return ");
                    String _simpleName_4 = f.getSimpleName();
                    _builder.append(_simpleName_4, "");
                    _builder.append(";");
                    _builder.newLineIfNotEmpty();
                    return _builder;
                  }
                };
                it.setBody(_function);
              }
            };
            cls.addMethod(_plus_1, _function_14);
            String _simpleName_1 = f.getSimpleName();
            String _firstUpper_1 = StringExtensions.toFirstUpper(_simpleName_1);
            String _plus_2 = ("set" + _firstUpper_1);
            final Procedure1<MutableMethodDeclaration> _function_15 = new Procedure1<MutableMethodDeclaration>() {
              @Override
              public void apply(final MutableMethodDeclaration it) {
                StringConcatenation _builder = new StringConcatenation();
                {
                  String _docComment = f.getDocComment();
                  boolean _defined = OptExtensions.<Object>defined(_docComment);
                  if (_defined) {
                    String _docComment_1 = f.getDocComment();
                    _builder.append(_docComment_1, "");
                  } else {
                    _builder.append("Set the value of the ");
                    String _simpleName = cls.getSimpleName();
                    _builder.append(_simpleName, "");
                    _builder.append(" entity property ");
                    String _simpleName_1 = f.getSimpleName();
                    _builder.append(_simpleName_1, "");
                    _builder.append(".");
                  }
                }
                _builder.newLineIfNotEmpty();
                _builder.append("<p>This will trigger a change event for the observers.");
                _builder.newLine();
                it.setDocComment(_builder.toString());
                boolean _isDeprecated = f.isDeprecated();
                it.setDeprecated(_isDeprecated);
                context.setPrimarySourceElement(it, f);
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
                  @Override
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
                              @Override
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
                                TypeReference _type_3 = f.getType();
                                TypeReference _newTypeReference_1 = context.newTypeReference(Cloneable.class);
                                boolean _extendsType = EntityProcessor.this.<Object>extendsType(_type_3, _newTypeReference_1);
                                if (_extendsType) {
                                  _builder.append("\t");
                                  _builder.append("getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, \"");
                                  String _simpleName_6 = f.getSimpleName();
                                  _builder.append(_simpleName_6, "\t");
                                  _builder.append("\", this.");
                                  String _simpleName_7 = f.getSimpleName();
                                  _builder.append(_simpleName_7, "\t");
                                  _builder.append(".clone()));");
                                  _builder.newLineIfNotEmpty();
                                } else {
                                  _builder.append("\t");
                                  _builder.append("getPublisher().apply(new Change(nl.kii.entity.ChangeType.UPDATE, \"");
                                  String _simpleName_8 = f.getSimpleName();
                                  _builder.append(_simpleName_8, "\t");
                                  _builder.append("\", this.");
                                  String _simpleName_9 = f.getSimpleName();
                                  _builder.append(_simpleName_9, "\t");
                                  _builder.append("));");
                                  _builder.newLineIfNotEmpty();
                                }
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
            cls.addMethod(_plus_2, _function_15);
          }
        }
        final Procedure1<MutableMethodDeclaration> _function_14 = new Procedure1<MutableMethodDeclaration>() {
          @Override
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
            context.setPrimarySourceElement(it, cls);
            final CompilationStrategy _function = new CompilationStrategy() {
              @Override
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
                {
                  TypeReference _newTypeReference = context.newTypeReference(Cloneable.class);
                  boolean _extendsType = EntityProcessor.this.<Object>extendsType(clsType, _newTypeReference);
                  if (_extendsType) {
                    _builder.append("\t\t");
                    String _name_1 = clsType.getName();
                    _builder.append(_name_1, "\t\t");
                    _builder.append(" value = ((");
                    String _name_2 = clsType.getName();
                    _builder.append(_name_2, "\t\t");
                    _builder.append(")change.getValue()).clone();");
                    _builder.newLineIfNotEmpty();
                  } else {
                    _builder.append("\t\t");
                    String _name_3 = clsType.getName();
                    _builder.append(_name_3, "\t\t");
                    _builder.append(" value = ((");
                    String _name_4 = clsType.getName();
                    _builder.append(_name_4, "\t\t");
                    _builder.append(")change.getValue());");
                    _builder.newLineIfNotEmpty();
                  }
                }
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
                _builder.append("\t\t");
                _builder.append("try {");
                _builder.newLine();
                _builder.append("\t\t\t");
                _builder.append("this.validate();");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("} catch(EntityException e) {");
                _builder.newLine();
                _builder.append("\t\t\t");
                _builder.append("throw new IllegalArgumentException(\"incoming change created an invalid entity: \" + change, e);");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("}");
                _builder.newLine();
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
        cls.addMethod("apply", _function_14);
        Iterable<? extends MutableMethodDeclaration> _declaredMethods = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_15 = new Function1<MutableMethodDeclaration, Boolean>() {
          @Override
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "toString"));
          }
        };
        Iterable<? extends MutableMethodDeclaration> _filter = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(_declaredMethods, _function_15);
        boolean _isEmpty = org.eclipse.xtext.xbase.lib.IterableExtensions.isEmpty(_filter);
        if (_isEmpty) {
          final Procedure1<MutableMethodDeclaration> _function_16 = new Procedure1<MutableMethodDeclaration>() {
            @Override
            public void apply(final MutableMethodDeclaration it) {
              context.setPrimarySourceElement(it, cls);
              TypeReference _string = context.getString();
              it.setReturnType(_string);
              final CompilationStrategy _function = new CompilationStrategy() {
                @Override
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
                        TypeReference _string = context.getString();
                        boolean _isAssignableFrom = _type.isAssignableFrom(_string);
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
          cls.addMethod("toString", _function_16);
        }
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_1 = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_17 = new Function1<MutableMethodDeclaration, Boolean>() {
          @Override
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "equals"));
          }
        };
        Iterable<? extends MutableMethodDeclaration> _filter_1 = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(_declaredMethods_1, _function_17);
        boolean _isEmpty_1 = org.eclipse.xtext.xbase.lib.IterableExtensions.isEmpty(_filter_1);
        if (_isEmpty_1) {
          final Procedure1<MutableMethodDeclaration> _function_18 = new Procedure1<MutableMethodDeclaration>() {
            @Override
            public void apply(final MutableMethodDeclaration it) {
              context.setPrimarySourceElement(it, cls);
              TypeReference _object = context.getObject();
              it.addParameter("object", _object);
              TypeReference _primitiveBoolean = context.getPrimitiveBoolean();
              it.setReturnType(_primitiveBoolean);
              final CompilationStrategy _function = new CompilationStrategy() {
                @Override
                public CharSequence compile(final CompilationStrategy.CompilationContext it) {
                  StringConcatenation _builder = new StringConcatenation();
                  _builder.append("if(object != null && object instanceof ");
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
                          boolean _or = false;
                          TypeReference _type_1 = field.getType();
                          TypeReference _newTypeReference = context.newTypeReference(Map.class);
                          boolean _extendsType = EntityProcessor.this.<Object>extendsType(_type_1, _newTypeReference);
                          if (_extendsType) {
                            _or = true;
                          } else {
                            TypeReference _type_2 = field.getType();
                            TypeReference _newTypeReference_1 = context.newTypeReference(List.class);
                            boolean _extendsType_1 = EntityProcessor.this.<Object>extendsType(_type_2, _newTypeReference_1);
                            _or = _extendsType_1;
                          }
                          if (_or) {
                            _builder.append("\t\t");
                            _builder.append("// consider an empty ");
                            TypeReference _type_3 = field.getType();
                            String _simpleName_3 = _type_3.getSimpleName();
                            _builder.append(_simpleName_3, "\t\t");
                            _builder.append(" the same as a null. one of the below must be true:");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("(");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("// both are null");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("(this.");
                            String _simpleName_4 = field.getSimpleName();
                            _builder.append(_simpleName_4, "\t\t\t");
                            _builder.append(" == null && ((");
                            String _simpleName_5 = cls.getSimpleName();
                            _builder.append(_simpleName_5, "\t\t\t");
                            _builder.append(") object).");
                            String _simpleName_6 = field.getSimpleName();
                            _builder.append(_simpleName_6, "\t\t\t");
                            _builder.append(" == null) ||");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("// or the this.");
                            String _simpleName_7 = field.getSimpleName();
                            _builder.append(_simpleName_7, "\t\t\t");
                            _builder.append(" is not null but empty, and object.");
                            String _simpleName_8 = field.getSimpleName();
                            _builder.append(_simpleName_8, "\t\t\t");
                            _builder.append(" is null ");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("(");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("this.");
                            String _simpleName_9 = field.getSimpleName();
                            _builder.append(_simpleName_9, "\t\t\t\t");
                            _builder.append(" != null && this.");
                            String _simpleName_10 = field.getSimpleName();
                            _builder.append(_simpleName_10, "\t\t\t\t");
                            _builder.append(".isEmpty() &&");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("((");
                            String _simpleName_11 = cls.getSimpleName();
                            _builder.append(_simpleName_11, "\t\t\t\t");
                            _builder.append(") object).");
                            String _simpleName_12 = field.getSimpleName();
                            _builder.append(_simpleName_12, "\t\t\t\t");
                            _builder.append(" == null");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append(") ||");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("// or the this.");
                            String _simpleName_13 = field.getSimpleName();
                            _builder.append(_simpleName_13, "\t\t\t");
                            _builder.append(" is null, and object.");
                            String _simpleName_14 = field.getSimpleName();
                            _builder.append(_simpleName_14, "\t\t\t");
                            _builder.append(" is not null but empty ");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("(");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("this.");
                            String _simpleName_15 = field.getSimpleName();
                            _builder.append(_simpleName_15, "\t\t\t\t");
                            _builder.append(" == null &&");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("((");
                            String _simpleName_16 = cls.getSimpleName();
                            _builder.append(_simpleName_16, "\t\t\t\t");
                            _builder.append(") object).");
                            String _simpleName_17 = field.getSimpleName();
                            _builder.append(_simpleName_17, "\t\t\t\t");
                            _builder.append(" != null &&");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("((");
                            String _simpleName_18 = cls.getSimpleName();
                            _builder.append(_simpleName_18, "\t\t\t\t");
                            _builder.append(") object).");
                            String _simpleName_19 = field.getSimpleName();
                            _builder.append(_simpleName_19, "\t\t\t\t");
                            _builder.append(".isEmpty()");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append(") ||");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("// or both are not null");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("(");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("this.");
                            String _simpleName_20 = field.getSimpleName();
                            _builder.append(_simpleName_20, "\t\t\t\t");
                            _builder.append(" != null && ");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("this.");
                            String _simpleName_21 = field.getSimpleName();
                            _builder.append(_simpleName_21, "\t\t\t\t");
                            _builder.append(".equals(((");
                            String _simpleName_22 = cls.getSimpleName();
                            _builder.append(_simpleName_22, "\t\t\t\t");
                            _builder.append(") object).");
                            String _simpleName_23 = field.getSimpleName();
                            _builder.append(_simpleName_23, "\t\t\t\t");
                            _builder.append(")");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append(") ");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append(")");
                            _builder.newLine();
                          } else {
                            _builder.append("\t\t");
                            _builder.append("(");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("(this.");
                            String _simpleName_24 = field.getSimpleName();
                            _builder.append(_simpleName_24, "\t\t\t");
                            _builder.append(" == null && ((");
                            String _simpleName_25 = cls.getSimpleName();
                            _builder.append(_simpleName_25, "\t\t\t");
                            _builder.append(") object).");
                            String _simpleName_26 = field.getSimpleName();
                            _builder.append(_simpleName_26, "\t\t\t");
                            _builder.append(" == null) ||");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t");
                            _builder.append("(");
                            _builder.newLine();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("this.");
                            String _simpleName_27 = field.getSimpleName();
                            _builder.append(_simpleName_27, "\t\t\t\t");
                            _builder.append(" != null && ");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t\t");
                            _builder.append("\t\t");
                            _builder.append("this.");
                            String _simpleName_28 = field.getSimpleName();
                            _builder.append(_simpleName_28, "\t\t\t\t");
                            _builder.append(".equals(((");
                            String _simpleName_29 = cls.getSimpleName();
                            _builder.append(_simpleName_29, "\t\t\t\t");
                            _builder.append(") object).");
                            String _simpleName_30 = field.getSimpleName();
                            _builder.append(_simpleName_30, "\t\t\t\t");
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
          cls.addMethod("equals", _function_18);
        }
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_2 = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_19 = new Function1<MutableMethodDeclaration, Boolean>() {
          @Override
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "hashCode"));
          }
        };
        Iterable<? extends MutableMethodDeclaration> _filter_2 = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(_declaredMethods_2, _function_19);
        boolean _isEmpty_2 = org.eclipse.xtext.xbase.lib.IterableExtensions.isEmpty(_filter_2);
        if (_isEmpty_2) {
          final Procedure1<MutableMethodDeclaration> _function_20 = new Procedure1<MutableMethodDeclaration>() {
            @Override
            public void apply(final MutableMethodDeclaration it) {
              context.setPrimarySourceElement(it, cls);
              TypeReference _primitiveInt = context.getPrimitiveInt();
              it.setReturnType(_primitiveInt);
              final CompilationStrategy _function = new CompilationStrategy() {
                @Override
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
                          boolean _or = false;
                          TypeReference _type_1 = field.getType();
                          TypeReference _newTypeReference = context.newTypeReference(Map.class);
                          boolean _extendsType = EntityProcessor.this.<Object>extendsType(_type_1, _newTypeReference);
                          if (_extendsType) {
                            _or = true;
                          } else {
                            TypeReference _type_2 = field.getType();
                            TypeReference _newTypeReference_1 = context.newTypeReference(List.class);
                            boolean _extendsType_1 = EntityProcessor.this.<Object>extendsType(_type_2, _newTypeReference_1);
                            _or = _extendsType_1;
                          }
                          if (_or) {
                            _builder.append("\t");
                            _builder.append("((this.");
                            String _simpleName_1 = field.getSimpleName();
                            _builder.append(_simpleName_1, "\t");
                            _builder.append(" != null) ?");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t");
                            _builder.append("\t");
                            _builder.append("(");
                            _builder.newLine();
                            _builder.append("\t");
                            _builder.append("\t\t");
                            _builder.append("this.");
                            String _simpleName_2 = field.getSimpleName();
                            _builder.append(_simpleName_2, "\t\t\t");
                            _builder.append(".isEmpty() ?");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t");
                            _builder.append("\t\t\t");
                            _builder.append("\"null\".hashCode()");
                            _builder.newLine();
                            _builder.append("\t");
                            _builder.append("\t\t\t");
                            _builder.append(": (this.");
                            String _simpleName_3 = field.getSimpleName();
                            _builder.append(_simpleName_3, "\t\t\t\t");
                            _builder.append(" + \"\").hashCode()");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t");
                            _builder.append("\t");
                            _builder.append(")");
                            _builder.newLine();
                            _builder.append("\t");
                            _builder.append("\t");
                            _builder.append(": 0)");
                            _builder.newLine();
                          } else {
                            _builder.append("\t");
                            _builder.append("((this.");
                            String _simpleName_4 = field.getSimpleName();
                            _builder.append(_simpleName_4, "\t");
                            _builder.append(" != null) ?");
                            _builder.newLineIfNotEmpty();
                            _builder.append("\t");
                            _builder.append("\t");
                            _builder.append("(this.");
                            String _simpleName_5 = field.getSimpleName();
                            _builder.append(_simpleName_5, "\t\t");
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
                  }
                  _builder.append(") * 37;");
                  _builder.newLine();
                  return _builder;
                }
              };
              it.setBody(_function);
            }
          };
          cls.addMethod("hashCode", _function_20);
        }
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_3 = cls.getDeclaredMethods();
        final Function1<MutableMethodDeclaration, Boolean> _function_21 = new Function1<MutableMethodDeclaration, Boolean>() {
          @Override
          public Boolean apply(final MutableMethodDeclaration it) {
            String _simpleName = it.getSimpleName();
            return Boolean.valueOf(Objects.equal(_simpleName, "clone"));
          }
        };
        Iterable<? extends MutableMethodDeclaration> _filter_3 = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(_declaredMethods_3, _function_21);
        boolean _isEmpty_3 = org.eclipse.xtext.xbase.lib.IterableExtensions.isEmpty(_filter_3);
        if (_isEmpty_3) {
          final Procedure1<MutableMethodDeclaration> _function_22 = new Procedure1<MutableMethodDeclaration>() {
            @Override
            public void apply(final MutableMethodDeclaration it) {
              context.setPrimarySourceElement(it, cls);
              it.setReturnType(clsType);
              final CompilationStrategy _function = new CompilationStrategy() {
                @Override
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
          cls.addMethod("clone", _function_22);
        }
        final Function1<MutableFieldDeclaration, Boolean> _function_23 = new Function1<MutableFieldDeclaration, Boolean>() {
          @Override
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            String _simpleName = _type.getSimpleName();
            return Boolean.valueOf(_simpleName.startsWith("Map"));
          }
        };
        Iterable<? extends MutableFieldDeclaration> _filter_4 = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(reactiveFields, _function_23);
        final Consumer<MutableFieldDeclaration> _function_24 = new Consumer<MutableFieldDeclaration>() {
          @Override
          public void accept(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
            final TypeReference key = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>head(_actualTypeArguments);
            TypeReference _type_1 = it.getType();
            List<TypeReference> _actualTypeArguments_1 = _type_1.getActualTypeArguments();
            final TypeReference value = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>last(_actualTypeArguments_1);
            TypeReference _newTypeReference = context.newTypeReference(EntityMap.class, key, value);
            it.setType(_newTypeReference);
          }
        };
        _filter_4.forEach(_function_24);
        final Function1<MutableFieldDeclaration, Boolean> _function_25 = new Function1<MutableFieldDeclaration, Boolean>() {
          @Override
          public Boolean apply(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            String _simpleName = _type.getSimpleName();
            return Boolean.valueOf(_simpleName.startsWith("List"));
          }
        };
        Iterable<? extends MutableFieldDeclaration> _filter_5 = org.eclipse.xtext.xbase.lib.IterableExtensions.filter(reactiveFields, _function_25);
        final Consumer<MutableFieldDeclaration> _function_26 = new Consumer<MutableFieldDeclaration>() {
          @Override
          public void accept(final MutableFieldDeclaration it) {
            TypeReference _type = it.getType();
            List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
            final TypeReference typeArg = _actualTypeArguments.get(0);
            TypeReference _newTypeReference = context.newTypeReference(EntityList.class, typeArg);
            it.setType(_newTypeReference);
          }
        };
        _filter_5.forEach(_function_26);
        final Procedure1<MutableMethodDeclaration> _function_27 = new Procedure1<MutableMethodDeclaration>() {
          @Override
          public void apply(final MutableMethodDeclaration it) {
            it.setVisibility(Visibility.PROTECTED);
            it.setDocComment("creates a listener for propagating to changes on a field to the publisher");
            TypeReference _string = context.getString();
            it.addParameter("path", _string);
            context.setPrimarySourceElement(it, cls);
            it.setReturnType(changeHandlerType);
            final CompilationStrategy _function = new CompilationStrategy() {
              @Override
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
        cls.addMethod("newChangeHandler", _function_27);
      }
    }
  }
  
  public boolean isReactive(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    boolean _xblockexpression = false;
    {
      TypeReference _type = field.getType();
      final Type type = _type.getType();
      boolean _xifexpression = false;
      if ((type instanceof AnnotationTarget)) {
        TypeReference _newTypeReference = context.newTypeReference(Entity.class);
        Type _type_1 = _newTypeReference.getType();
        AnnotationReference _findAnnotation = ((AnnotationTarget)type).findAnnotation(_type_1);
        _xifexpression = (!Objects.equal(_findAnnotation, null));
      }
      final boolean isEntity = _xifexpression;
      boolean _or = false;
      boolean _or_1 = false;
      if (isEntity) {
        _or_1 = true;
      } else {
        TypeReference _type_2 = field.getType();
        TypeReference _newTypeReference_1 = context.newTypeReference(List.class);
        boolean _extendsType = this.<Object>extendsType(_type_2, _newTypeReference_1);
        _or_1 = _extendsType;
      }
      if (_or_1) {
        _or = true;
      } else {
        TypeReference _type_3 = field.getType();
        TypeReference _newTypeReference_2 = context.newTypeReference(Map.class);
        boolean _extendsType_1 = this.<Object>extendsType(_type_3, _newTypeReference_2);
        _or = _extendsType_1;
      }
      _xblockexpression = _or;
    }
    return _xblockexpression;
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
  
  public <T extends Object> boolean extendsType(final TypeReference type, final TypeReference superType) {
    return superType.isAssignableFrom(type);
  }
  
  public CharSequence assignFieldValue(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _isEntityList = this.isEntityList(field);
      if (_isEntityList) {
        _builder.append("// if the list is not already reactive, wrap the list as a reactive list");
        _builder.newLine();
        CharSequence _newEntityList = this.newEntityList(field, "newList", context);
        _builder.append(_newEntityList, "");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
        _builder.append("if(value != null) newList.addAll(value);");
        _builder.newLine();
        String _simpleName = field.getSimpleName();
        _builder.append(_simpleName, "");
        _builder.append(" = newList;");
        _builder.newLineIfNotEmpty();
        _builder.append("this.");
        String _stopObservingFunctionName = this.getStopObservingFunctionName(field);
        _builder.append(_stopObservingFunctionName, "");
        _builder.append(" = newList.onChange(newChangeHandler(\"");
        String _simpleName_1 = field.getSimpleName();
        _builder.append(_simpleName_1, "");
        _builder.append("\"));");
        _builder.newLineIfNotEmpty();
      } else {
        boolean _isEntityMap = this.isEntityMap(field);
        if (_isEntityMap) {
          _builder.append("// if the map is not already listenable, wrap the map as a listenable");
          _builder.newLine();
          CharSequence _newEntityMap = this.newEntityMap(field, "newMap", context);
          _builder.append(_newEntityMap, "");
          _builder.append(";");
          _builder.newLineIfNotEmpty();
          _builder.append("if(value != null) newMap.putAll(value);");
          _builder.newLine();
          String _simpleName_2 = field.getSimpleName();
          _builder.append(_simpleName_2, "");
          _builder.append(" = newMap;");
          _builder.newLineIfNotEmpty();
          _builder.append("this.");
          String _stopObservingFunctionName_1 = this.getStopObservingFunctionName(field);
          _builder.append(_stopObservingFunctionName_1, "");
          _builder.append(" = newMap.onChange(newChangeHandler(\"");
          String _simpleName_3 = field.getSimpleName();
          _builder.append(_simpleName_3, "");
          _builder.append("\"));");
          _builder.newLineIfNotEmpty();
        } else {
          boolean _isObservable = this.isObservable(field, context);
          if (_isObservable) {
            _builder.append("this.");
            String _simpleName_4 = field.getSimpleName();
            _builder.append(_simpleName_4, "");
            _builder.append(" = value;");
            _builder.newLineIfNotEmpty();
            _builder.append("if(value != null) this.");
            String _stopObservingFunctionName_2 = this.getStopObservingFunctionName(field);
            _builder.append(_stopObservingFunctionName_2, "");
            _builder.append(" = this.");
            String _simpleName_5 = field.getSimpleName();
            _builder.append(_simpleName_5, "");
            _builder.append(".onChange(newChangeHandler(\"");
            String _simpleName_6 = field.getSimpleName();
            _builder.append(_simpleName_6, "");
            _builder.append("\"));");
            _builder.newLineIfNotEmpty();
          } else {
            _builder.append("this.");
            String _simpleName_7 = field.getSimpleName();
            _builder.append(_simpleName_7, "");
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
  
  public CharSequence newEntityList(final MutableFieldDeclaration field, final String valName, @Extension final TransformationContext context) {
    CharSequence _xblockexpression = null;
    {
      TypeReference _type = field.getType();
      List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
      final TypeReference valueType = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>head(_actualTypeArguments);
      final TypeReference type = context.newTypeReference(EntityList.class, valueType);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("final ");
      String _simpleName = type.getSimpleName();
      _builder.append(_simpleName, "");
      _builder.append(" ");
      _builder.append(valName, "");
      _builder.append(" = new ");
      String _name = type.getName();
      _builder.append(_name, "");
      _builder.append("(");
      String _simpleName_1 = valueType.getSimpleName();
      _builder.append(_simpleName_1, "");
      _builder.append(".class);");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  public CharSequence newEntityMap(final MutableFieldDeclaration field, final String valName, @Extension final TransformationContext context) {
    CharSequence _xblockexpression = null;
    {
      TypeReference _type = field.getType();
      List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
      final TypeReference keyType = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>head(_actualTypeArguments);
      TypeReference _type_1 = field.getType();
      List<TypeReference> _actualTypeArguments_1 = _type_1.getActualTypeArguments();
      final TypeReference valueType = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>last(_actualTypeArguments_1);
      final TypeReference type = context.newTypeReference(EntityMap.class, keyType, valueType);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("final ");
      String _simpleName = type.getSimpleName();
      _builder.append(_simpleName, "");
      _builder.append(" ");
      _builder.append(valName, "");
      _builder.append(" = new ");
      String _name = type.getName();
      _builder.append(_name, "");
      _builder.append("(");
      String _simpleName_1 = keyType.getSimpleName();
      _builder.append(_simpleName_1, "");
      _builder.append(".class, ");
      String _simpleName_2 = valueType.getSimpleName();
      _builder.append(_simpleName_2, "");
      _builder.append(".class);");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  public TypeReference toEntityMapType(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    TypeReference _xblockexpression = null;
    {
      TypeReference _type = field.getType();
      List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
      final TypeReference keyType = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>head(_actualTypeArguments);
      TypeReference _type_1 = field.getType();
      List<TypeReference> _actualTypeArguments_1 = _type_1.getActualTypeArguments();
      final TypeReference valueType = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>last(_actualTypeArguments_1);
      _xblockexpression = context.newTypeReference(EntityMap.class, keyType, valueType);
    }
    return _xblockexpression;
  }
  
  public TypeReference toEntityListType(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    TypeReference _type = field.getType();
    List<TypeReference> _actualTypeArguments = _type.getActualTypeArguments();
    TypeReference _head = org.eclipse.xtext.xbase.lib.IterableExtensions.<TypeReference>head(_actualTypeArguments);
    return context.newTypeReference(EntityList.class, _head);
  }
  
  public void addClassTypeParameters(final MutableExecutableDeclaration constructor, final ClassDeclaration cls, @Extension final TransformationContext context) {
    Iterable<? extends TypeParameterDeclaration> _typeParameters = cls.getTypeParameters();
    final Procedure2<TypeParameterDeclaration, Integer> _function = new Procedure2<TypeParameterDeclaration, Integer>() {
      @Override
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
        @Override
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
        @Override
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
  
  public String getNameWithoutGenerics(final TypeReference ref) {
    String _name = ref.getName();
    return _name.replaceAll("<.+>", "");
  }
}
